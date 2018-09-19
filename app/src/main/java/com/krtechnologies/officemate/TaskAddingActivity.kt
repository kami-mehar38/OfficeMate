package com.krtechnologies.officemate

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import com.krtechnologies.officemate.helpers.Validator
import com.krtechnologies.officemate.models.Task
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_task_adding.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.json.JSONObject

class TaskAddingActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        val EXTRA_TASK = "TASK"
    }

    private var task: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_adding)

        btnAddTask.setOnClickListener {
            addTask()
        }
    }

    private fun addTask() {
        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(false)
        dialog.setMessage("Adding task...")
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel") { _, button ->
            if (button == DialogInterface.BUTTON_POSITIVE)
                AndroidNetworking.cancel("task")
        }

        if (Validator.getInstance().validateText(etTitle.text.toString().trim())) {
            if (Validator.getInstance().validateText(etDescription.text.toString().trim())) {
                dialog.show()
                val request = ANRequest.PostRequestBuilder<ANRequest.PatchRequestBuilder>("${Helper.BASE_URL}/task").apply {
                    addBodyParameter("title", etTitle.text.toString().trim())
                    addBodyParameter("description", etDescription.text.toString().trim())
                    addBodyParameter("email", PreferencesManager.getInstance().getUserEmail())
                    if (PreferencesManager.getInstance().getIsAdmin())
                        addBodyParameter("admin_email", PreferencesManager.getInstance().getUserEmail())
                    else addBodyParameter("admin_email", PreferencesManager.getInstance().getUserAdminEmail())
                    setTag("task")
                    setPriority(Priority.HIGH)
                }

                val anRequest = request.build()
                anRequest.getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        dialog.dismiss()
                        when (response?.getInt("status")) {
                            201 -> {
                                task = Helper.getInstance().getGson().fromJson(response.getJSONObject("task").toString(), Task::class.java)
                                info { task.toString() }
                                Toasty.success(this@TaskAddingActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show()
                                onBackPressed()
                            }
                            202 -> Toasty.error(this@TaskAddingActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show()
                            203 -> Toasty.info(this@TaskAddingActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show()
                        }
                    }

                    override fun onError(anError: ANError?) {
                        dialog.dismiss()
                        Toasty.error(this@TaskAddingActivity, "Failed to add task", Toast.LENGTH_SHORT, true).show()
                    }
                })
            } else Toasty.error(this, "Enter task description", Toast.LENGTH_SHORT, true).show()
        } else Toasty.error(this, "Enter task title", Toast.LENGTH_SHORT, true).show()
    }

    override fun onBackPressed() {
        sendBackData()
    }

    private fun sendBackData() {
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_TASK, task)
        if (task != null)
            setResult(Activity.RESULT_OK, returnIntent)
        else setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }
}
