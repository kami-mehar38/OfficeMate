package com.krtechnologies.officemate

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making the status bar transparent
        setStatusBarColor()

        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }
}
