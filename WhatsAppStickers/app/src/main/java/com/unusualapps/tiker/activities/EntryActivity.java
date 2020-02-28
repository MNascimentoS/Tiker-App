package com.unusualapps.tiker.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.unusualapps.tiker.R;
import com.unusualapps.tiker.constants.Constants;
import com.unusualapps.tiker.identities.StickerPacksContainer;
import com.unusualapps.tiker.utils.FileUtils;
import com.unusualapps.tiker.utils.StickerPacksManager;
import com.unusualapps.tiker.whatsapp_api.AddStickerPackActivity;
import com.unusualapps.tiker.whatsapp_api.Sticker;
import com.unusualapps.tiker.whatsapp_api.StickerContentProvider;
import com.unusualapps.tiker.whatsapp_api.StickerPack;
import com.unusualapps.tiker.whatsapp_api.StickerPackValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EntryActivity extends AddStickerPackActivity {
    private View progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void insertStickerPackInContentProvider(StickerPack stickerPack) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("stickerPack", new Gson().toJson(stickerPack));
        getContentResolver().insert(StickerContentProvider.AUTHORITY_URI, contentValues);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    private void initialize() {
        setContentView(R.layout.activity_entry);
        overridePendingTransition(0, 0);
        Intent intent = getIntent();
        Fresco.initialize(this);
        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));


        // Figure out what to do based on the intent type
        if (intent.getType() != null){
            if (intent.getType().equals("text/*")) {
                ArrayList<Sticker> stickers = new ArrayList<Sticker>();
                //stickerPack.trayImageFile = "ICONE_DO_GRUPO";
                //stickerPack.name = "Nome do Grupo";
                //addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
                Uri uri = intent.getClipData().getItemAt(0).getUri();
                String name2 = getFileName(uri);
                String name = name2.replace("Conversa do WhatsApp com ","").replace(".txt","");
                StickerPacksManager.deleteStickerPack(name);

                List<Uri> uries = new ArrayList<>();
                for (int i = 1; i < intent.getClipData().getItemCount(); i++) {
                    Uri uristiker = intent.getClipData().getItemAt(i).getUri();
                    if (!uristiker.toString().endsWith(".webp")) {
                        throw new IllegalStateException("O item na posição" + i + "não é uma imagem");
                    }
                    uries.add(uristiker);
                }
                final StickerPack stickerPack = new StickerPack(name, name, "MrMenezes", "", "", "", "", "");
                stickerPack.setAndroidPlayStoreLink("");
                stickerPack.setIosAppStoreLink("");

                //Save the sticker images locally and get the list of new stickers for pack
                List<Sticker> stickerList = null;
                String stickerPath = Constants.STICKERS_DIRECTORY_PATH + name;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    stickerList = StickerPacksManager.saveStickerPackFilesLocally(name, uries, this);
                }
                stickerPack.setStickers(stickerList);

                //Generate image tray icon
                String trayIconFile = FileUtils.generateRandomIdentifier() + ".png";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    StickerPacksManager.createStickerPackTrayIconFile(uries.get(0), Uri.parse(stickerPath + "/" + trayIconFile), this);
                }
                stickerPack.trayImageFile = trayIconFile;

                //Save stickerPack created to write in json
                StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack);
                StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer);
                insertStickerPackInContentProvider(stickerPack);

                StickerPackValidator.verifyStickerPackValidity(this, stickerPack);
                this.addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

}
