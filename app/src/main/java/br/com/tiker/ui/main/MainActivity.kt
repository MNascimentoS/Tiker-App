package br.com.tiker.ui.main

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import br.com.tiker.R
import br.com.tiker.ui.allStickers.AllStickersFragment
import br.com.tiker.ui.entry.EntryActivity
import kotlinx.android.synthetic.main.activity_entry.viewPager
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private lateinit var adapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
        initListeners()
        viewModel.removeAllUnsavedStickers()
        viewModel.retriveIntentData(this, intent, contentResolver)
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        tabLayout?.setupWithViewPager(viewPager)

        adapter = MainPagerAdapter(this, supportFragmentManager)
        viewPager?.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.import_whats_app) {
            startActivity(Intent(this, EntryActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListeners() {
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        val img = resources.getDrawable(R.drawable.ic_add, theme)
                        principalBTN?.apply {
                            text = getString(R.string.create_package)
                            setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)
                            setOnClickListener {
                                if (::adapter.isInitialized) {
                                    val allFragment = adapter.getItem(0) as AllStickersFragment
                                    allFragment.createPackage()
                                }
                            }
                        }
                    }
                    1 -> {
                        principalBTN?.apply {
                            text = getString(R.string.share_with_your_friends)
                            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                            setOnClickListener {
                                val sendIntent = Intent()
                                sendIntent.action = Intent.ACTION_SEND
                                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_to_friends_message))
                                sendIntent.type = "text/plain"
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                startActivity(shareIntent)
                            }
                        }
                    }
                }

                val anim = if (position == 0) ValueAnimator.ofInt(principalBTN.width, resources.getDimension(R.dimen.createPackageWidth).toInt())
                else ValueAnimator.ofInt(principalBTN.width, resources.getDimension(R.dimen.shareWithFriendsWidth).toInt())

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
}
