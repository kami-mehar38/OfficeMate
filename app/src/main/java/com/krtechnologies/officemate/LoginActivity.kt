package com.krtechnologies.officemate

import android.annotation.TargetApi
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
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
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.io.File
import java.util.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.krtechnologies.officemate.R.string.email
import org.jetbrains.anko.toast


class LoginActivity : AppCompatActivity() {

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun logIn() {

        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(false)
        dialog.setMessage("Logging in...")
        /*if (Validator.getInstance().validateEmail(etEmail.text.toString().trim())) {
            if (Validator.getInstance().validatePassword(etPassword.text.toString().trim())) {
                dialog.show()
                AndroidNetworking.get("http://10.0.2.2:8000/api/login/${etEmail.text.toString().trim()}/${etPassword.text.toString().trim()}")
                        .setTag("login")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                dialog.dismiss()
                                when (response?.getInt("status")) {
                                    201 -> {
                                        val admin = Helper.getInstance().getGson().fromJson(response.getJSONObject("admin").toString(), Admin::class.java)
                                        PreferencesManager.getInstance().saveUser(admin)
                                        PreferencesManager.getInstance().setLogInStatus(true)
                                        Toasty.success(this@LoginActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show();
                                        startActivity<HomeActivity>()
                                    }
                                    202 -> Toasty.error(this@LoginActivity, response.getString("message"), Toast.LENGTH_SHORT, true).show();
                                }
                            }

                            override fun onError(anError: ANError?) {
                                dialog.dismiss()
                                Toasty.error(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT, true).show();
                            }

                        })
            } else Toasty.error(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT, true).show();
        } else Toasty.error(this, "Invalid email", Toast.LENGTH_SHORT, true).show();
    */
        Helper.getInstance().getFirebaseAuthInstance().signInWithEmailAndPassword(etEmail.text.toString().trim(), etPassword.text.toString().trim())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = Helper.getInstance().getFirebaseAuthInstance().currentUser
                        toast(user?.email!!)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}
