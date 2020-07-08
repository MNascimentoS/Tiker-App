package br.com.tiker.ui.splashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
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

import br.com.tiker.R;
import br.com.tiker.persistence.FirebaseDB;
import br.com.tiker.scene.main.ui.EntryActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        mRetryBTN = findViewById(R.id.retryAllowAccessBTN);
        ExtensionsKt.fadeIn(findViewById(R.id.logoTickerContainer), AnimationConstants.DURATION_LONG, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        FileUtils.initializeDirectories(this);
        if (RequestPermissionsHelper.verifyPermissions(this)) {

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }, 1000);

//            FirebaseUser currentUser = mAuth.getCurrentUser();
//            if (currentUser != null) {

//            } else configureLoginButton();
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
            Toast.makeText(this, "We need access to write and read files in your phone", Toast.LENGTH_SHORT).show();
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
                ExtensionsKt.alert(this, "Erro", "Houve um erro ao tentar realizar o login do usuário");
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
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            FirebaseDB.Companion.addOrUpdateUser(user.getUid());
                        }
                        startActivity(new Intent(SplashScreenActivity.this, EntryActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                    mRetryBTN.setClickable(true);
                })
                .addOnCanceledListener(this, () -> {
                    ExtensionsKt.alert(this, "Erro", "O login foi cancelado");
                    mRetryBTN.setClickable(true);
                    mAuth.signOut();
                })
                .addOnFailureListener(this, error -> {
                    Sentry.capture(error);
                    ExtensionsKt.alert(this, "Erro", "Houve um erro ao tentar realizar o login");
                    mRetryBTN.setClickable(true);
                    mAuth.signOut();
                });
    }
}
