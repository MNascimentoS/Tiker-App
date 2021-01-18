package br.com.tiker.ui.main

import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import br.com.tiker.BuildConfig
import br.com.tiker.R
import br.com.tiker.persistence.FirebaseDB
import br.com.tiker.ui.allStickers.AllStickersFragment
import br.com.tiker.ui.becomePremium.BecomePremiumActivity
import br.com.tiker.ui.entry.EntryActivity
import br.com.tiker.ui.splashScreen.SplashScreenActivity
import br.com.tiker.utils.alert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_entry.viewPager
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private lateinit var adapter: MainPagerAdapter
    private var addedOnMenu: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
        initListeners()
        if (!BuildConfig.DEBUG) {
            Sentry.init(getString(R.string.sentry_dns))
        }
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        tabLayout?.setupWithViewPager(viewPager)

        adapter = MainPagerAdapter(this, supportFragmentManager)
        viewPager?.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        addedOnMenu = false
        invalidateOptionsMenu()
        viewModel.removeAllUnsavedStickers()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        FirebaseDB.getUserIsPremium { isPremium ->
            if (addedOnMenu) return@getUserIsPremium
            if (!isPremium) menu?.add(Menu.NONE, BECOME_PREMIUM, Menu.NONE, getString(R.string.become_premium))
            menu?.add(Menu.NONE, LOGOUT, Menu.NONE, getString(R.string.logout))
            addedOnMenu = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.import_whats_app -> startActivity(Intent(this, EntryActivity::class.java))
            R.id.rate_us -> {
                val appPackageName = packageName
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (ex: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
            BECOME_PREMIUM -> {
                startActivity(Intent(this, BecomePremiumActivity::class.java))
            }
            LOGOUT -> {
                alert(getString(R.string.are_you_sure), getString(R.string.you_can_login_again), getString(R.string.yes), true) {
                    FirebaseAuth.getInstance().signOut()
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListeners() {
        principalBTN?.setOnClickListener { createPackage() }
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        val img = ResourcesCompat.getDrawable(resources, R.drawable.ic_add, theme)
                        principalBTN?.apply {
                            text = getString(R.string.create_package)
                            setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)
                            setOnClickListener { createPackage() }
                        }
                    }
                    1 -> {
                        principalBTN?.apply {
                            text = getString(R.string.share_with_your_friends)
                            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                            setOnClickListener { shareWithFriends() }
                        }
                    }
                }

                val animValue = resources.getDimension(if (position == 0) R.dimen.createPackageWidth
                else R.dimen.shareWithFriendsWidth).toInt()

                val anim = ValueAnimator.ofInt(principalBTN.width, animValue)

                anim.addUpdateListener { animation ->
                    val layoutParams = principalBTN.layoutParams
                    layoutParams.width = animation.animatedValue as Int
                    principalBTN.requestLayout()
                }
                anim.duration = 250
                anim.start()
            }
        })
    }

    private fun createPackage() {
        if (::adapter.isInitialized) {
            val allFragment = adapter.getItem(0) as AllStickersFragment
            allFragment.createPackage()
        }
    }

    private fun shareWithFriends() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_to_friends_message))
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    companion object {
        private const val BECOME_PREMIUM = 75623
        private const val LOGOUT = 23245
    }
}
