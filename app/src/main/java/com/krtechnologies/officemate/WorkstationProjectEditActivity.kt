package com.krtechnologies.officemate

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.SeekBar
import com.krtechnologies.officemate.models.WorkstationProject
import kotlinx.android.synthetic.main.activity_workstation_project_edit.*


class WorkstationProjectEditActivity : AppCompatActivity() {

    companion object {
        const val KEY_EXTRA_PROJECT = "EXTRA_PROJECT"
    }

    private var workstationProject: WorkstationProject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workstation_project_edit)

        // setting the toolbar
        setSupportActionBar(toolbar)

        // receiving the project object
        if (intent.hasExtra(KEY_EXTRA_PROJECT)) {
            workstationProject = intent.extras.get(KEY_EXTRA_PROJECT) as WorkstationProject
        }

        initViews()
    }

    private fun initViews() {
        ivBack.setOnClickListener {
            onBackPressed()
        }
        workstationProject?.let {
            tvTitle.text = it.projectName
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                tvProgress.text = resources.getString(R.string.completed).plus(" $p1%")
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })
    }

    private fun animateProgress(progress: Int) {
        val animProgress = ValueAnimator.ofInt(0, progress)
        animProgress.addUpdateListener {
            val updatedProgress = it.animatedValue as Int
            seekBar.progress = updatedProgress
        }
        animProgress.duration = 1500
        animProgress.interpolator = AccelerateDecelerateInterpolator()
        animProgress.start()
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        animateProgress(76)
    }
}
