package com.krtechnologies.officemate.fragments


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.krtechnologies.officemate.App
import com.krtechnologies.officemate.MemberSelectingActivity
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.models.Employee
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_workstation_fragment_for_admin.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.json.JSONObject

class WorkstationFragmentForAdmin : Fragment(), AnkoLogger {

    companion object {
        const val REQUEST_CODE_MEMBER_SELECT = 1
    }

    private var employee: Employee? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workstation_fragment_for_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAssignTo.setOnClickListener {
            startActivityForResult(Intent(context, MemberSelectingActivity::class.java), REQUEST_CODE_MEMBER_SELECT)
        }

        ivRemoveEmployee.setOnClickListener {
            employee = null
            popUpAssignButton()
        }

        btnAssignProject.setOnClickListener {
            postProject()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WorkstationFragmentForAdmin.REQUEST_CODE_MEMBER_SELECT && resultCode == Activity.RESULT_OK) {
            data?.let {
                employee = it.getSerializableExtra(MemberSelectingActivity.EXTRA_EMPLOYEE) as Employee
                info { employee }
                employee?.run {
                    popUpChip()
                }
            }

        }
    }

    private fun popUpChip() {

        val animScaleX = ObjectAnimator.ofFloat(sendToChip, View.SCALE_X.name, 0f, 1f)
        val animScaleY = ObjectAnimator.ofFloat(sendToChip, View.SCALE_Y.name, 0f, 1f)

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
                if (sendToChip.visibility != View.VISIBLE)
                    sendToChip.visibility = View.VISIBLE
                if (btnAssignTo.visibility != View.GONE)
                    btnAssignTo.visibility = View.GONE
                employee?.run {
                    tvEmployeeName.text = name
                }
            }
        })
        animatorSet.start()
    }

    private fun popUpAssignButton() {

        val animScaleX = ObjectAnimator.ofFloat(btnAssignTo, View.SCALE_X.name, 0f, 1f)
        val animScaleY = ObjectAnimator.ofFloat(btnAssignTo, View.SCALE_Y.name, 0f, 1f)

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
                if (btnAssignTo.visibility != View.VISIBLE)
                    btnAssignTo.visibility = View.VISIBLE
                if (sendToChip.visibility != View.GONE)
                    sendToChip.visibility = View.GONE
                employee?.run {
                    tvEmployeeName.text = name
                }
            }
        })
        animatorSet.start()
    }

    private fun postProject() {
        if (etProjectTitle.text.toString().trim().isNotEmpty()) {
            if (etProjectDescription.text.toString().trim().isNotEmpty()) {
                if (employee != null) {
                    employee?.run {
                        val dialog = ProgressDialog(context)
                        dialog.isIndeterminate = true
                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        dialog.setCancelable(false)
                        dialog.setTitle("Sign Up")
                        dialog.setMessage("Assigning the project...")
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel") { _, button ->
                            if (button == DialogInterface.BUTTON_POSITIVE)
                                AndroidNetworking.cancel("assign")
                        }

                        dialog.show()
                        AndroidNetworking.post("${Helper.BASE_URL}/project")
                                .addBodyParameter("project_name", etProjectTitle.text.toString().trim())
                                .addBodyParameter("project_description", etProjectDescription.text.toString().trim())
                                .addBodyParameter("eta", "0 days")
                                .addBodyParameter("completion", "0 %")
                                .addBodyParameter("assigned_to", name)
                                .addBodyParameter("email", email)
                                .addBodyParameter("admin_email", adminEmail)
                                .addBodyParameter("profile_picture", profilePicture)
                                .setTag("assign")
                                .setPriority(Priority.MEDIUM)
                                .build()
                                .getAsJSONObject(object : JSONObjectRequestListener {
                                    override fun onResponse(response: JSONObject) {
                                        dialog.dismiss()
                                        info { response }
                                        if (response.has("status")) {
                                            val status = response.getInt("status")
                                            when (status) {
                                                201 -> Toasty.success(App.getAppContext(), response.getString("message"))
                                                202 -> Toasty.success(App.getAppContext(), response.getString("message"))
                                            }
                                        }
                                    }

                                    override fun onError(error: ANError) {
                                        dialog.dismiss()
                                        Toasty.error(App.getAppContext(), "Error occurred while assigning project")
                                    }
                                })
                    }
                } else Toasty.error(App.getAppContext(), "Select employee to assign project")
            } else Toasty.error(App.getAppContext(), "Enter project description")
        } else Toasty.error(App.getAppContext(), "Enter project title")
    }

}
