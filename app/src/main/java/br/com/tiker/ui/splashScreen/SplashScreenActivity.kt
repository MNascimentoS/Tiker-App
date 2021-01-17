package br.com.tiker.ui.splashScreen

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.tiker.BuildConfig
import br.com.tiker.R
import br.com.tiker.persistence.FirebaseDB.Companion.addOrUpdateUser
import br.com.tiker.persistence.FirebaseDB.Companion.getUserIsPremium
import br.com.tiker.ui.main.MainActivity
import br.com.tiker.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity(R.layout.activity_splash_screen) {

    private var firebaseAuth: FirebaseAuth? = null

    private var logoIsBig = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCenter.start(application, getString(R.string.appcenter_key),
                Analytics::class.java, Crashes::class.java, Distribute::class.java)
        androidVersionTXT.text = (BuildConfig.VERSION_NAME + " : " + BuildConfig.VERSION_CODE)
        firebaseAuth = FirebaseAuth.getInstance()
        initUi()
    }

    override fun onStart() {
        super.onStart()
        FileUtils.initializeDirectories(this)
        if (RequestPermissionsHelper.verifyPermissions(this)) {
            firebaseAuth?.currentUser?.let {
                getUserIsPremium { isPremium: Boolean -> checkUserIsPremium(isPremium) }
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                    finish()
                }, 1500)
            } ?: run {
                configureLoginButton()
            }
        } else {
            RequestPermissionsHelper.requestPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        FileUtils.initializeDirectories(this)
        if (RequestPermissionsHelper.verifyPermissions(this)) { //If the app has all the required permissions we pass to MainActivity to get started
            configureLoginButton()
        } else {
            Toast.makeText(this, R.string.eror_access_read_files, Toast.LENGTH_SHORT).show()
            configureTryAccessButton()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Sentry.capture(e)
                // Google Sign In failed, update UI appropriately
                this.alert(getString(R.string.error), getString(R.string.error_login_user))
                firebaseAuth?.signOut()
                retryAllowAccessBTN.isClickable = true
            }
        }
    }

    private fun initUi() {
        logoTickerContainer.fadeIn(AnimationConstants.DURATION_LONG, null)
        animButton(resources.getDimension(R.dimen.tikerLogoSmall).toInt(), resources.getDimension(R.dimen.tikerLogoBig).toInt())
    }

    private fun animButton(fromSize: Int, toSize: Int) {
        val anim = ValueAnimator.ofInt(fromSize, toSize)
        anim.addUpdateListener { animation: ValueAnimator ->
            val layoutParams = logoTickerContainer.layoutParams
            val animationValue = animation.animatedValue as Int
            if (animationValue == toSize) {
                anim.cancel()
                logoIsBig = !logoIsBig
                val dimen: Int = if (logoIsBig) resources.getDimension(R.dimen.tikerLogoSmall).toInt() else resources.getDimension(R.dimen.tikerLogoBig).toInt()
                animButton(animationValue, dimen)
                return@addUpdateListener
            }
            layoutParams.width = animationValue
            layoutParams.height = animationValue
            logoTickerContainer.requestLayout()
        }
        anim.duration = 1500
        anim.start()
    }

    private fun checkUserIsPremium(isPremium: Boolean) {
        firebaseAuth?.currentUser?.let {
            addOrUpdateUser(it.uid, isPremium)
        }
    }

    private fun configureLoginButton() {
        retryAllowAccessBTN.text = getString(R.string.do_login)
        retryAllowAccessBTN.setOnClickListener { signIn() }
        withoutLoginTXT.visibility = View.VISIBLE
        withoutLoginTXT.setOnClickListener { notNowLogin() }
        retryAllowAccessBTN.fadeIn(AnimationConstants.DURATION_LONG, null)
    }

    private fun configureTryAccessButton() {
        retryAllowAccessBTN.setOnClickListener { RequestPermissionsHelper.requestPermissions(this) }
        retryAllowAccessBTN.fadeIn(AnimationConstants.DURATION_LONG, null)
    }

    private fun signIn() {
        retryAllowAccessBTN.isClickable = false
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun notNowLogin() {
        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        finish()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        getUserIsPremium { isPremium: Boolean -> checkUserIsPremium(isPremium) }
                        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                        finish()
                    }
                    retryAllowAccessBTN.isClickable = true
                }
                ?.addOnCanceledListener(this) {
                    this.alert(getString(R.string.error), getString(R.string.error_login_canceled))
                    retryAllowAccessBTN.isClickable = true
                    firebaseAuth?.signOut()
                }
                ?.addOnFailureListener(this) { error: Exception? ->
                    Sentry.capture(error)
                    this.alert(getString(R.string.error), getString(R.string.error_login_default))
                    retryAllowAccessBTN.isClickable = true
                    firebaseAuth?.signOut()
                }
    }

    companion object {
        const val RC_SIGN_IN = 871
    }

}