package com.krtechnologies.officemate.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.krtechnologies.officemate.fragments.FragmentIntroductorySlides

/**
 * Created by ingizly on 8/6/18
 **/

class SlidesViewPagerAdapter(supportFragmentManager: FragmentManager) : FragmentStatePagerAdapter(supportFragmentManager) {

    private val numberOfPages: Int = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FragmentIntroductorySlides.getInstance(position)
            1 -> FragmentIntroductorySlides.getInstance(position)
            else -> {
                FragmentIntroductorySlides.getInstance(position)
            }
        }
    }

    override fun getCount(): Int = numberOfPages

}