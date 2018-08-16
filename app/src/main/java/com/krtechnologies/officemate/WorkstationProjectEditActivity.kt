package com.krtechnologies.officemate

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.krtechnologies.officemate.models.WorkstationProject
import devlight.io.library.ArcProgressStackView
import kotlinx.android.synthetic.main.activity_workstation_project_edit.*


class WorkstationProjectEditActivity : AppCompatActivity() {

    companion object {
        public val KEY_EXTRA_PROJECT = "EXTRA_PROJECT"
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

        val models = ArrayList<ArcProgressStackView.Model>()
        models.apply {
            add(ArcProgressStackView.Model("Progress", 25f, Color.WHITE, ContextCompat.getColor(this@WorkstationProjectEditActivity, R.color.colorAccent)))
            add(ArcProgressStackView.Model("Progress", 50f, Color.WHITE, ContextCompat.getColor(this@WorkstationProjectEditActivity, R.color.colorAccent)))
        }
        circleProgress.models = models
    }
}
