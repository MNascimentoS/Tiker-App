package br.com.tiker.scene.main.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import br.com.tiker.R

class TutorialViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> TutorialFragment.newInstance(R.layout.fragment_tutorial_one)
            1 -> TutorialFragment.newInstance(R.layout.fragment_tutorial_two)
            2 -> TutorialFragment.newInstance(R.layout.fragment_tutorial_three)
            3 -> TutorialFragment.newInstance(R.layout.fragment_tutorial_four)
            4 -> TutorialFragment.newInstance(R.layout.fragment_tutorial_five)
            5 -> TutorialFragment.newInstance(R.layout.fragment_tutorial_six)
            else -> Fragment()
        }

    override fun getCount(): Int = 6

}