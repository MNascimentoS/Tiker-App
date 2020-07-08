package br.com.tiker.ui.main

import br.com.tiker.R
import br.com.tiker.ui.allStickers.AllStickersFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.activity_create.createPackageBTN
import kotlinx.android.synthetic.main.activity_entry.viewPager
import kotlinx.android.synthetic.main.activity_main.*


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

    private fun initListeners() {
        createPackageBTN?.setOnClickListener {
            if (::adapter.isInitialized) {
                val allFragment = adapter.getItem(0) as AllStickersFragment
                allFragment.createPackage()
            }
        }
    }
}
