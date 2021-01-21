package br.com.tiker.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import br.com.tiker.R
import br.com.tiker.ui.allStickers.AllStickersFragment
import br.com.tiker.ui.myPackages.MyPackagesFragment

class MainPagerAdapter(context: Context, supportFragmentManager: FragmentManager) :
    FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentTitles = listOf(
        context.getString(R.string.label_all_stickers),
        context.getString(R.string.label_my_packages)
    )
    private val fragments = arrayListOf<Fragment>()

    init {
        fragments.add(AllStickersFragment.newInstance())
        fragments.add(MyPackagesFragment.newInstance())
    }

    override fun getPageTitle(position: Int): String = fragmentTitles[position]

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

}