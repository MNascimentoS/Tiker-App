package br.com.tiker.ui.splashScreen;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import br.com.tiker.BuildConfig;
import br.com.tiker.R;
import br.com.tiker.persistence.FirebaseDB;
import br.com.tiker.ui.entry.EntryActivity;
import br.com.tiker.ui.main.MainActivity;
import br.com.tiker.utils.AnimationConstants;
import br.com.tiker.utils.ExtensionsKt;
import br.com.tiker.utils.FileUtils;
import br.com.tiker.utils.RequestPermissionsHelper;
import io.sentry.Sentry;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 871;
    private static final String TAG = SplashScreenActivity.class.getName();

    private FirebaseAuth mAuth;// ...

    private Button mRetryBTN;
    private TextView mNotNowTXT;
    private Boolean logoIsBig = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        TextView androidVersion = findViewById(R.id.androidVersionTXT);
        androidVersion.setText((BuildConfig.VERSION_NAME + " : " + BuildConfig.VERSION_CODE));
        mAuth = FirebaseAuth.getInstance();
        mRetryBTN = findViewById(R.id.retryAllowAccessBTN);
        mNotNowTXT = findViewById(R.id.withoutLoginTXT);

        initUi();
    }

    private void initUi() {
        ImageView tickerLogo = findViewById(R.id.logoTickerContainer);
        ExtensionsKt.fadeIn(tickerLogo, AnimationConstants.DURATION_LONG, null);
        animButton((int) getResources().getDimension(R.dimen.tikerLogoSmall), (int) getResources().getDimension(R.dimen.tikerLogoBig));
    }

    private void animButton(int fromSize, int toSize) {
        ImageView tickerLogo = findViewById(R.id.logoTickerContainer);
        ValueAnimator anim = ValueAnimator.ofInt(fromSize, toSize);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = tickerLogo.getLayoutParams();
            int animationValue = (int) animation.getAnimatedValue();
            if (animationValue == toSize) {
                anim.cancel();
                logoIsBig = !logoIsBig;
                int dimen;
                if (logoIsBig) dimen = (int) getResources().getDimension(R.dimen.tikerLogoSmall);
                else dimen = (int) getResources().getDimension(R.dimen.tikerLogoBig);
                animButton(animationValue, dimen);
                return;
            }
            layoutParams.width = animationValue;
            layoutParams.height = animationValue;
            tickerLogo.requestLayout();
        });
        anim.setDuration(1500);
        anim.start();
    }

    private kotlin.Unit checkUserIsPremium(boolean isPremium) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseDB.Companion.addOrUpdateUser(currentUser.getUid(), isPremium);
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        FileUtils.initializeDirectories(this);
        if (RequestPermissionsHelper.verifyPermissions(this)) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                FirebaseDB.Companion.getUserIsPremium(this::checkUserIsPremium);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    finish();
                }, 1500);
            } else configureLoginButton();
        } else {
            RequestPermissionsHelper.requestPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FileUtils.initializeDirectories(this);
        if (RequestPermissionsHelper.verifyPermissions(this)) {//If the app has all the required permissions we pass to MainActivity to get started
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();
            //            configureLoginButton();
        } else {
            Toast.makeText(this, R.string.eror_access_read_files, Toast.LENGTH_SHORT).show();
            configureTryAccessButton();
        }
    }

    private void configureTryAccessButton() {
        mRetryBTN.setOnClickListener(v -> RequestPermissionsHelper.requestPermissions(this));
        ExtensionsKt.fadeIn(mRetryBTN, AnimationConstants.DURATION_LONG, null);
    }

    private void configureLoginButton() {
        mRetryBTN.setText(getString(R.string.do_login));
        mRetryBTN.setOnClickListener(v -> signIn());
        mNotNowTXT.setVisibility(View.VISIBLE);
        mNotNowTXT.setOnClickListener(v -> notNowLogin());
        ExtensionsKt.fadeIn(mRetryBTN, AnimationConstants.DURATION_LONG, null);
    }

    private void signIn() {
        mRetryBTN.setClickable(false);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void notNowLogin() {
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Sentry.capture(e);
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                ExtensionsKt.alert(this, getString(R.string.error), getString(R.string.error_login_user));
                mAuth.signOut();
                mRetryBTN.setClickable(true);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseDB.Companion.getUserIsPremium(this::checkUserIsPremium);
                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                    mRetryBTN.setClickable(true);
                })
                .addOnCanceledListener(this, () -> {
                    ExtensionsKt.alert(this, getString(R.string.error), getString(R.string.error_login_canceled));
                    mRetryBTN.setClickable(true);
                    mAuth.signOut();
                })
                .addOnFailureListener(this, error -> {
                    Sentry.capture(error);
                    ExtensionsKt.alert(this, getString(R.string.error), getString(R.string.error_login_default));
                    mRetryBTN.setClickable(true);
                    mAuth.signOut();
                });
    }
}
