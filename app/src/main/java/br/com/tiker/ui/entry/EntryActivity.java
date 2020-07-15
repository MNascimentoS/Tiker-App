package br.com.tiker.ui.entry;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import br.com.tiker.BuildConfig;
import br.com.tiker.R;
import br.com.tiker.model.Constants;
import br.com.tiker.persistence.StickerRoomDatabase;
import br.com.tiker.persistence.model.StickerEntity;
import br.com.tiker.persistence.model.StickerPackageEntity;
import br.com.tiker.ui.base.AddStickerPackActivity;
import br.com.tiker.model.Sticker;
import br.com.tiker.model.StickerPack;
import br.com.tiker.ui.tutorial.TutorialViewPagerAdapter;
import br.com.tiker.utils.StickerPackValidator;
import br.com.tiker.utils.StickerPacksContainer;
import br.com.tiker.utils.ExtensionsKt;
import br.com.tiker.utils.FileUtils;
import br.com.tiker.utils.StickerPacksManager;
import br.com.tiker.services.StickerContentProvider;
import io.sentry.Sentry;
import kotlin.Lazy;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static br.com.tiker.utils.StickerPackValidator.STICKER_SIZE_MAX;
import static org.koin.java.KoinJavaComponent.inject;


public class EntryActivity extends AddStickerPackActivity implements RewardedVideoAdListener {
    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private final static int MAX_ITEMS = 5;

     private Lazy<StickerRoomDatabase> stickerDb = inject(StickerRoomDatabase.class);


    private RewardedVideoAd mRewardedVideoAd;
    private Button mShareWithFriend;
    private CircularProgressBar mProgress;
    private TextView mProgressText;
    private ViewPager mViewPager;
    private View mProgressComponentRL;
    private int currentTime = 6;
    private int currentProgress = 100;
    private boolean stickerListEmpty = false;
    private boolean adCompleted = false;
    private String error = null;
    private ArrayList<StickerEntity> imageByteList = new ArrayList<StickerEntity>();
    private String name = "";


    private ArrayList<StickerPack> stickerPack = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        Sentry.init(getString(R.string.sentry_dns));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColor(R.color.backgroundSecondary));
        }

        MobileAds.initialize(this, initializationStatus -> {
        });

        mShareWithFriend = findViewById(R.id.shareWithFriendsBTN);

        mProgressComponentRL = findViewById(R.id.progressComponentRL);
        mViewPager = findViewById(R.id.viewPager);
        mProgress = findViewById(R.id.circularProgressBar);
        mProgressText = findViewById(R.id.textProgressTXT);
        mViewPager.setAdapter(new TutorialViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentProgress = 100;
                currentTime = 6;
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);
        dotsIndicator.setViewPager(mViewPager);

        configureChangePage();

        checkPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PACK) {
            if (resultCode == RESULT_CANCELED) {
                if (data != null) {
                    final String validationError = data.getStringExtra("validation_error");
                    if (validationError != null) {
                        Log.e("AddStickerPackActivity", "Validation failed:" + validationError);
                    }
                } else {
                    new StickerPackNotAddedMessageFragment().show(getSupportFragmentManager(), "sticker_pack_not_added");
                }
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        stickerDb.getValue().stickerDao().addStickerListJ(name, getString(R.string.app_name), new StickerPackageEntity(), imageByteList);
                    }
                }.start();
                ExtensionsKt.alert(this, getString(R.string.sticker_added), getString(R.string.back_whatsapp_see_package), getString(R.string.back), true, null, () -> {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                    startActivity(launchIntent);
                    return null;
                });
            }
        }
    }

    private void configureChangePage() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (currentTime == 0) {
                currentProgress = 100;
                currentTime = 6;
                if (mViewPager.getCurrentItem() == MAX_ITEMS) {
                    mProgress.setVisibility(View.INVISIBLE);
                    mProgressText.setVisibility(View.INVISIBLE);
                    return;
                } else {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                }
            }
            currentTime -= 1;
            mProgress.setProgress(currentProgress);
            currentProgress -= 20;
            mProgressText.setText(String.valueOf(currentTime));
            configureChangePage();
        }, 1000);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
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
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
//                if (currentUser == null) {
//                    startActivity(new Intent(this, RequestPermissionActivity.class));
//                    finish();
//                } else {
                initialize();
//                }
                break;
        }
    }

    private void initialize() {
        Intent intent = getIntent();
        Fresco.initialize(this);
        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));

        // Figure out what to do based on the intent type
        if (intent.getType() != null) {
            ExtensionsKt.alert(this, getString(R.string.adding_package), getString(R.string.info_watch_add), getString(R.string.watch), false, null, () -> {
                mProgressComponentRL.setVisibility(View.VISIBLE);
                new Thread() {
                    @Override
                    public void run() {
                        createPackage();
                    }
                }.start();
                String ad = "";
                if (BuildConfig.DEBUG) ad = getString(R.string.cod_ad_debug);
                else ad = getString(R.string.cod_ad_release);
                mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
                mRewardedVideoAd.setRewardedVideoAdListener(this);
                mRewardedVideoAd.loadAd(ad, new AdRequest.Builder().build());
                return null;
            });
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initListeners();
    }

    private void initListeners() {
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
            ArrayList<Sticker> stickers = new ArrayList();
            Uri uri = intent.getClipData().getItemAt(0).getUri();
            String name2 = getFileName(uri);
            name = name2.replace(getString(R.string.whatsapp_conversation), "").replace(".txt", "");
            StickerPacksManager.deleteStickerPack(name);
            for (int i = 1; i < 10; i++) {
                StickerPacksManager.deleteStickerPack(name + " " + i);
            }
            List<Uri> uries = new ArrayList<>();
            for (int i = 1; i < intent.getClipData().getItemCount(); i++) {
                Uri uristiker = intent.getClipData().getItemAt(i).getUri();
                if (!uristiker.toString().endsWith(".webp")) continue;

                uries.add(uristiker);
            }
            if (uries.isEmpty()) {
                stickerListEmpty = true;
                return;
            } else {
                stickerListEmpty = false;
            }

            ArrayList<String> dividedListName = new ArrayList();
            ArrayList<List<Uri>> dividedList = new ArrayList();
            if (uries.size() > STICKER_SIZE_MAX) {
                dividedList = ExtensionsKt.chopped(uries, STICKER_SIZE_MAX);
                for (int i = dividedList.size(); i > 0; i--) {
                    dividedListName.add(name + " " + i);
                }
            } else {
                dividedList.add(uries);
                dividedListName.add(name);
            }

            for (int i = 0; i < dividedList.size(); i++) {
                stickerPack.add(new StickerPack(dividedListName.get(i), dividedListName.get(i), getString(R.string.app_name), "", "tickerapp0@gmail.com", "", "", ""));
                stickerPack.get(i).setAndroidPlayStoreLink("");
                stickerPack.get(i).setIosAppStoreLink("");

                //Save the sticker images locally and get the list of new stickers for pack
                List<Sticker> stickerList;
                String stickerPath = Constants.STICKERS_DIRECTORY_PATH + dividedListName.get(i);
                stickerList = StickerPacksManager.saveStickerPackFilesLocally(dividedListName.get(i), dividedList.get(i), this);
                stickerPack.get(i).setStickers(stickerList);

                //Generate image tray icon
                String trayIconFile = FileUtils.generateRandomIdentifier() + ".png";
                StickerPacksManager.createStickerPackTrayIconFile(dividedList.get(i).get(0), Uri.parse(stickerPath + "/" + trayIconFile), this);

                stickerPack.get(i).trayImageFile = trayIconFile;

                //Save stickerPack created to write in json
                StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack.get(i));
                StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer);
                insertStickerPackInContentProvider(stickerPack.get(i));

                try {
                    StickerPackValidator.verifyStickerPackValidity(this, stickerPack.get(i));
                } catch (Exception ex) {
                    error = ex.getMessage();
                }

                ContentResolver cr = getApplicationContext().getContentResolver();
                for (int j = 0; j < dividedList.get(i).size(); j++) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    try {
                        InputStream is = cr.openInputStream(dividedList.get(i).get(j));
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        imageByteList.add(new StickerEntity(0, stream.toByteArray()));
                        if (is != null) is.close();
                    } catch (IOException e) {
                        Sentry.capture(e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }



    @Override
    public void onRewardedVideoAdLoaded() {
        mProgressComponentRL.setVisibility(View.GONE);

        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        if (!adCompleted) {
            if (error == null) {
                ExtensionsKt.alert(this, getString(R.string.error), getString(R.string.error_watch_add), getString(R.string.watch), false, null, () -> {
                    mProgressComponentRL.setVisibility(View.VISIBLE);
                    String ad = "";
                    if (BuildConfig.DEBUG) ad = getString(R.string.cod_ad_debug);
                    else ad = getString(R.string.cod_ad_release);
                    mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
                    mRewardedVideoAd.setRewardedVideoAdListener(this);
                    mRewardedVideoAd.loadAd(ad, new AdRequest.Builder().build());
                    return null;
                });
            } else {
                Sentry.capture(error);
                ExtensionsKt.alert(this, getString(R.string.error), getString(R.string.error_default), getString(R.string.ok), false, null, () -> null);
            }
        } else {
            if (stickerListEmpty) {
                ExtensionsKt.alert(this, getString(R.string.error), getString(R.string.error_find_stickers), getString(R.string.ok), false, null, () -> null);
            } else {
                for (int i = 0; i < stickerPack.size(); i++) {
                    this.addStickerPackToWhatsApp(stickerPack.get(i).identifier, stickerPack.get(i).name);
                }
            }
        }
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int value) {
        for (int i = 0; i < stickerPack.size(); i++) {
            this.addStickerPackToWhatsApp(stickerPack.get(i).identifier, stickerPack.get(i).name);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        adCompleted = true;
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }
}
