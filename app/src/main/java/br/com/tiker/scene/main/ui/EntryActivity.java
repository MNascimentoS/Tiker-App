package br.com.tiker.scene.main.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.reward.AdMetadataListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import br.com.tiker.BuildConfig;
import br.com.tiker.R;
import br.com.tiker.old.constants.Constants;
import br.com.tiker.old.identities.StickerPacksContainer;
import br.com.tiker.scene.main.ui.BecomePremiumActivity;
import br.com.tiker.scene.requestPermissions.RequestPermissionActivity;
import br.com.tiker.utils.ExtensionsKt;
import br.com.tiker.utils.FileUtils;
import br.com.tiker.utils.StickerPacksManager;
import br.com.tiker.old.whatsapp_api.AddStickerPackActivity;
import br.com.tiker.old.whatsapp_api.Sticker;
import br.com.tiker.old.whatsapp_api.StickerContentProvider;
import br.com.tiker.old.whatsapp_api.StickerPack;
import br.com.tiker.old.whatsapp_api.StickerPackValidator;
import io.sentry.Sentry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private RewardedVideoAd mRewardedVideoAd;
    private Button mShareWithFriend;
    private CircularProgressBar mProgress;
    private TextView mProgressText;
    private ViewPager mViewPager;
    private View mProgressComponentRL;
    private int currentTime = 6;
    private int currentProgress = 100;
    private boolean adCompleted = false;

    private StickerPack stickerPack;

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
            public void onPageSelected(int position) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);
        dotsIndicator.setViewPager(mViewPager);

        configureChangePage();

        checkPermissions();
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
            ExtensionsKt.alert(this, "Adicionando Pacote", "Antes de adicionar o pacote, assista esta propaganda para ajudar o projeto.", "Assistir", () -> {
                mProgressComponentRL.setVisibility(View.VISIBLE);
                createPackage();
                String ad = "";
                if (BuildConfig.DEBUG) ad = "ca-app-pub-3940256099942544/5224354917";
                else ad = "ca-app-pub-3355203749923756/1018041110";
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
            ArrayList<Sticker> stickers = new ArrayList<Sticker>();
            Uri uri = intent.getClipData().getItemAt(0).getUri();
            String name2 = getFileName(uri);
            String name = name2.replace("Conversa do WhatsApp com ", "").replace(".txt", "");
            StickerPacksManager.deleteStickerPack(name);

            List<Uri> uries = new ArrayList<>();
            for (int i = 1; i < intent.getClipData().getItemCount(); i++) {
                Uri uristiker = intent.getClipData().getItemAt(i).getUri();
                if (!uristiker.toString().endsWith(".webp")) {
//                    throw new IllegalStateException("O item na posição" + i + "não é uma imagem");
                    continue;
                }
                uries.add(uristiker);
            }
            stickerPack = new StickerPack(name, name, "Tiker", "", "tickerapp0@gmail.com", "", "", "");
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
    public void onRewardedVideoAdOpened() {}

    @Override
    public void onRewardedVideoStarted() {}

    @Override
    public void onRewardedVideoAdClosed() {
        if (!adCompleted) {
            ExtensionsKt.alert(this, "Erro", "Você precisa assistir a propaganda até o final", "Assistir", () -> {
                mProgressComponentRL.setVisibility(View.VISIBLE);
                String ad = "";
                if (BuildConfig.DEBUG) ad = "ca-app-pub-3940256099942544/5224354917";
                else ad = "ca-app-pub-3355203749923756/1018041110";
                mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
                mRewardedVideoAd.setRewardedVideoAdListener(this);
                mRewardedVideoAd.loadAd(ad, new AdRequest.Builder().build());
                return null;
            });
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        this.addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        this.addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
    }

    @Override
    public void onRewardedVideoCompleted() {
        adCompleted = true;
    }
}
