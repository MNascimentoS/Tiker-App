package br.com.tiker.scene.main.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import br.com.tiker.BuildConfig;
import br.com.tiker.R;
import br.com.tiker.old.constants.Constants;
import br.com.tiker.old.identities.StickerPacksContainer;
import br.com.tiker.scene.main.ui.BecomePremiumActivity;
import br.com.tiker.utils.FileUtils;
import br.com.tiker.utils.StickerPacksManager;
import br.com.tiker.old.whatsapp_api.AddStickerPackActivity;
import br.com.tiker.old.whatsapp_api.Sticker;
import br.com.tiker.old.whatsapp_api.StickerContentProvider;
import br.com.tiker.old.whatsapp_api.StickerPack;
import br.com.tiker.old.whatsapp_api.StickerPackValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntryActivity extends AddStickerPackActivity {
    private View progressBar;

    private InterstitialAd mInterstitialAd;
    private Button mBecomePremium;
    private Button mShareWithFriend;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColor(R.color.backgroundSecondary));
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

        mBecomePremium = findViewById(R.id.becomePremiumBTN);
        mShareWithFriend = findViewById(R.id.shareWithFriendsBTN);

        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new TutorialViewPagerAdapter(getSupportFragmentManager()));

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
        Intent intent = getIntent();
        Fresco.initialize(this);
        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));

        // Figure out what to do based on the intent type
        if (intent.getType() != null) {
            mInterstitialAd = new InterstitialAd(this);
            String ad = "";
            if (BuildConfig.DEBUG) ad = "ca-app-pub-3940256099942544/1033173712";
            else ad = "ca-app-pub-3355203749923756/3144521325";
            mInterstitialAd.setAdUnitId(ad);
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) { createPackage(); }

                @Override
                public void onAdClosed() { createPackage(); }

                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(new AdRequest
                    .Builder()
                    .addTestDevice("60B9B4742AC9143603341A6AC6C538E1")
                    .build()
            );
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initListeners();
    }

    private void initListeners() {
        mBecomePremium.setOnClickListener(v -> startActivity(new Intent(this, BecomePremiumActivity.class)));
        mShareWithFriend.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_to_friends_message));
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }

    private void createPackage() {
        Intent intent = getIntent();
        if (intent.getType().equals("text/*")) {
            ArrayList<Sticker> stickers = new ArrayList<Sticker>();
            //stickerPack.trayImageFile = "ICONE_DO_GRUPO";
            //stickerPack.name = "Nome do Grupo";
            //addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
            Uri uri = intent.getClipData().getItemAt(0).getUri();
            String name2 = getFileName(uri);
            String name = name2.replace("Conversa do WhatsApp com ", "").replace(".txt", "");
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

}
