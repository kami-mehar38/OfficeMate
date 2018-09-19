package com.krtechnologies.officemate

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
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
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doFromSdk
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.json.JSONObject


class LoginActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making the status bar transparent
        setStatusBarColor()

        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            logIn()
        }

        tvSignUp.setOnClickListener {
            startActivity<SignUpActivity>()
        }

    }


    @SuppressLint("NewApi")
    private fun setStatusBarColor() {
        doFromSdk(Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun logIn() {

        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(false)
        dialog.setMessage("Logging in...")
        if (Validator.getInstance().validateEmail(etEmail.text.toString().trim())) {
            if (Validator.getInstance().validatePassword(etPassword.text.toString().trim())) {
                dialog.show()
                AndroidNetworking.get("${Helper.BASE_URL}/login/${etEmail.text.toString().trim()}/${etPassword.text.toString().trim()}")
                        .setTag("login")

                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                dialog.dismiss()
                                info { response }
                                when (response?.getInt("status")) {
                                    201 -> {
                                        if (response.has("admin")) {
                                            val admin = Helper.getInstance().getGson().fromJson(response.getJSONObject("admin").toString(), Admin::class.java)
                                            PreferencesManager.getInstance().saveUser(admin)
                                            PreferencesManager.getInstance().setLogInStatus(true)
                                        } else if (response.has("employee")) {
                                            val employee = Helper.getInstance().getGson().fromJson(response.getJSONObject("employee").toString(), Employee::class.java)
                                            info { employee.toString() }
                                            PreferencesManager.getInstance().saveUser(employee)
                                            PreferencesManager.getInstance().setLogInStatus(true)
                                        }
                                        Toasty.success(this@LoginActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show();
                                        startActivity<HomeActivity>()
                                    }
                                    202 -> Toasty.error(this@LoginActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show();
                                }
                            }

                            override fun onError(anError: ANError?) {
                                info { anError?.message }
                                dialog.dismiss()
                                Toasty.error(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT, true).show();
                            }

                        })
            } else Toasty.error(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT, true).show();
        } else Toasty.error(this, "Invalid email", Toast.LENGTH_SHORT, true).show();

    }
}
