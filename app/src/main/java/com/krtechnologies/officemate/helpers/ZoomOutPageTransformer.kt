package com.krtechnologies.officemate.helpers

import android.support.v4.view.ViewPager
import android.view.View


/**
 * Created by ingizly on 8/7/18
 **/

class ZoomOutPageTransformer : ViewPager.PageTransformer {

    companion object {
        const val MIN_SCALE = 0.85f
        const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.alpha = 0f
        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
            val vertMargin = pageHeight * (1 - scaleFactor) / 2
            val horzMargin = pageWidth * (1 - scaleFactor) / 2
            if (position < 0) {
                page.translationX = horzMargin - vertMargin / 2
            } else {
                page.translationX = -horzMargin + vertMargin / 2
            }

            // Scale the page down (between MIN_SCALE and 1)
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor

            // Fade the page relative to its size.
            page.alpha = MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                    (1 - MIN_SCALE) * (1 - MIN_ALPHA)

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            page.alpha = 0f
        }
    }

}