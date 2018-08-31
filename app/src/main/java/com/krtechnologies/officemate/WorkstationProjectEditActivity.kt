package com.krtechnologies.officemate

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import com.krtechnologies.officemate.helpers.Validator
import com.krtechnologies.officemate.models.Admin
import com.krtechnologies.officemate.models.Employee
import com.krtechnologies.officemate.models.Project
import com.krtechnologies.officemate.models.WorkstationProject
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_workstation_project_edit.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject


class WorkstationProjectEditActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        const val KEY_EXTRA_PROJECT = "EXTRA_PROJECT"
    }

    private var project: Project? = null
    private var completion = ""
    private var eta = ""

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
            etProjectDescription.setText(projectDescription)
            animateProgress(completion.split(" ")[0].toInt())
            numberPicker.value = eta.split(" ")[0].toInt()
            this@WorkstationProjectEditActivity.completion = completion
            this@WorkstationProjectEditActivity.eta = eta
            info { eta.split(" ")[0].toInt() }
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
                .addBodyParameter("project_description", etProjectDescription.text.toString().trim())
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
                            201 -> Toasty.error(this@WorkstationProjectEditActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show();
                            202 -> Toasty.error(this@WorkstationProjectEditActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show();
                        }
                    }

                    override fun onError(anError: ANError?) {
                        dialog.dismiss()
                        Toasty.error(this@WorkstationProjectEditActivity, "Update failed", Toast.LENGTH_SHORT, true).show();
                    }

                })

    }
}
