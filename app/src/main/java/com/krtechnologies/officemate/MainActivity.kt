package com.krtechnologies.officemate

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.krtechnologies.officemate.adapters.SlidesViewPagerAdapter
import com.krtechnologies.officemate.helpers.ZoomOutPageTransformer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var viewPager: ViewPager? = null
    private var slidesViewPagerAdapter: SlidesViewPagerAdapter? = null
    private var slidesIndicatorContainer: LinearLayout? = null
    private var list: ArrayList<View>? = null
    private var previousIndex: Int = 0
    private var currentSize: Int = 0
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hiding the status bar
        hideStatusBar()

        setContentView(R.layout.activity_main)

        // initializing the list of views of indicator container
        list = ArrayList()

        viewPager = findViewById(R.id.pager)
        viewPager?.setPageTransformer(true, ZoomOutPageTransformer())

        slidesViewPagerAdapter = SlidesViewPagerAdapter(supportFragmentManager)
        viewPager?.adapter = slidesViewPagerAdapter

        slidesIndicatorContainer = findViewById(R.id.slidesIndicatorContainer)

        for (index in 0..slidesIndicatorContainer!!.childCount) {
            list?.add(slidesIndicatorContainer!!.getChildAt(index))
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {

                animateIndicatorsSelected(list!![position])
                animateIndicatorsDeselected(list!![previousIndex])

                previousIndex = position
            }

        })

        // selecting the first indicator
        animateIndicatorsSelected(list!![0])

        handler = Handler()
        runnable = Runnable { hideStatusBar() }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler?.postDelayed(runnable, 1000)
            }
        }

    }

    private fun hideStatusBar() {

        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            // Hide the status bar.
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0)
            super.onBackPressed()
        else pager.currentItem = pager.currentItem - 1
    }

    fun animateIndicatorsSelected(view: View) {

        // getting the background of view
        val backgroundDrawable = view.background as GradientDrawable

        // changing the width and height of the indicators

        view.postDelayed({
            val animHeight = ValueAnimator.ofInt(view.measuredHeight, view.measuredHeight + 14)

            val animWidth = ValueAnimator.ofInt(view.measuredWidth, view.measuredWidth + 14)

            val animColor = ObjectAnimator.ofObject(backgroundDrawable, "color", ArgbEvaluator(), Color.parseColor("#4DDDDDDD"), Color.parseColor("#99aa00"))


            animHeight.addUpdateListener { valueAnimator ->
                val `val` = valueAnimator.animatedValue as Int
                val layoutParams = view.layoutParams
                layoutParams.height = `val`
                view.layoutParams = layoutParams
            }

            animWidth.addUpdateListener { valueAnimator ->
                val `val` = valueAnimator.animatedValue as Int
                val layoutParams = view.layoutParams
                layoutParams.width = `val`
                view.layoutParams = layoutParams
                currentSize = `val`
            }

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animHeight, animWidth, animColor)
            animatorSet.duration = 400
            animatorSet.start()
        }, 1)

    }

    fun animateIndicatorsDeselected(view: View) {

        // getting the background of view
        val backgroundDrawable = view.background as GradientDrawable

        // changing the width and height of the indicators
        view.postDelayed({
            val animHeight = ValueAnimator.ofInt(view.measuredHeight, view.measuredHeight - 14)

            val animWidth = ValueAnimator.ofInt(view.measuredWidth, view.measuredWidth - 14)

            val animColor = ObjectAnimator.ofObject(backgroundDrawable, "color", ArgbEvaluator(), Color.parseColor("#99aa00"), Color.parseColor("#4DDDDDDD"))

            animHeight.addUpdateListener { valueAnimator ->
                val `val` = valueAnimator.animatedValue as Int
                val layoutParams = view.layoutParams
                layoutParams.height = `val`
                view.layoutParams = layoutParams
            }

            animWidth.addUpdateListener { valueAnimator ->
                val `val` = valueAnimator.animatedValue as Int
                val layoutParams = view.layoutParams
                layoutParams.width = `val`
                view.layoutParams = layoutParams
            }

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animHeight, animWidth, animColor)
            animatorSet.duration = 400
            animatorSet.start()
        }, 1)

    }

    override fun onPause() {
        super.onPause()
        handler?.removeCallbacks(runnable)
    }

}
