package com.krtechnologies.officemate

import android.animation.ValueAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.krtechnologies.officemate.fragments.WorkstationFragment.Companion.KEY_EXTRA_PROJECT
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.models.Project
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_workstation_project_edit.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.json.JSONObject


class WorkstationProjectEditActivity : AppCompatActivity(), AnkoLogger {

    private var project: Project? = null
    private var completion = ""
    private var eta = ""
    private var shouldSendResultBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workstation_project_edit)

        // setting the toolbar
        setSupportActionBar(toolbar)

        // receiving the project object
        if (intent.hasExtra(KEY_EXTRA_PROJECT)) {
            project = intent.extras.get(KEY_EXTRA_PROJECT) as Project
        }

        initViews()
    }

    private fun initViews() {
        ivBack.setOnClickListener {
            onBackPressed()
        }

        numberPicker.minValue = 0
        numberPicker.maxValue = 60
        numberPicker.setOnScrollListener { numberPicker, _ ->
            tvETA.text = resources.getString(R.string.eta).run {
                if (numberPicker.value > 1) {
                    eta = " ${numberPicker.value} days"
                    plus(" ${numberPicker.value} days")

                } else {
                    eta = " ${numberPicker.value} day"
                    plus(" ${numberPicker.value} day")
                }
            }
        }

        project?.run {
            tvTitle.text = projectName
            etDescription.setText(projectDescription)
            animateProgress(completion.trim().split(" ")[0].toInt())
            numberPicker.value = eta.trim().split(" ")[0].toInt()
            this@WorkstationProjectEditActivity.completion = completion
            this@WorkstationProjectEditActivity.eta = eta

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                tvProgress.text = resources.getString(R.string.completed).plus(" $p1%")
                completion = " $p1 %"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        btnUpdate.setOnClickListener { _ ->
            project?.let {
                updateProject()
            }
        }
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
        project?.run {
        }
    }

    private fun updateProject() {


        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(false)
        dialog.setMessage("Updating project...")

        dialog.show()
        AndroidNetworking.post("${Helper.BASE_URL}/update/project")
                .setTag("login")
                .addBodyParameter("project_description", etDescription.text.toString().trim())
                .addBodyParameter("completion", completion)
                .addBodyParameter("eta", eta)
                .addBodyParameter("id", project!!.id)
                .addBodyParameter("email", project!!.email)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        dialog.dismiss()
                        info { response }
                        when (response?.getInt("status")) {
                            201 -> {
                                Toasty.success(this@WorkstationProjectEditActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show()
                                project?.let {
                                    it.projectDescription = etDescription.text.toString().trim()
                                    it.completion = completion
                                    it.eta = eta
                                    shouldSendResultBack = true
                                }
                            }
                            202 -> {
                                Toasty.error(this@WorkstationProjectEditActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show()
                                shouldSendResultBack = false
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        dialog.dismiss()
                        Toasty.error(this@WorkstationProjectEditActivity, "Update failed", Toast.LENGTH_SHORT, true).show();
                    }

                })

    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(KEY_EXTRA_PROJECT, project)
        when (shouldSendResultBack) {
            true -> setResult(Activity.RESULT_OK, intent)
            false -> setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }
}
