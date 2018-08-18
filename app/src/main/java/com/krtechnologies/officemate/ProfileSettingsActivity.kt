package com.krtechnologies.officemate

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlinx.android.synthetic.main.activity_profile_settings.*

class ProfileSettingsActivity : AppCompatActivity() {

    private var isAnimationFinished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        btnProfilePicture.setOnClickListener {

        }
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        popUp()
    }

    override fun onBackPressed() {
        if (isAnimationFinished)
            super.onBackPressed()
        else popDown()
    }

    private fun popUp() {

        val animScaleX = ObjectAnimator.ofFloat(btnProfilePicture, View.SCALE_X.name, 0f, 1f)
        val animScaleY = ObjectAnimator.ofFloat(btnProfilePicture, View.SCALE_Y.name, 0f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animScaleX, animScaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = OvershootInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                if (btnProfilePicture.visibility == View.GONE)
                    btnProfilePicture.visibility = View.VISIBLE
            }

        })
        animatorSet.start()

    }

    private fun popDown() {

        val animScaleX = ObjectAnimator.ofFloat(btnProfilePicture, View.SCALE_X.name, 1f, 0f)
        val animScaleY = ObjectAnimator.ofFloat(btnProfilePicture, View.SCALE_Y.name, 1f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animScaleX, animScaleY)
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (btnProfilePicture.visibility == View.VISIBLE)
                    btnProfilePicture.visibility = View.GONE
                isAnimationFinished = true
                onBackPressed()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        animatorSet.start()
    }
}
