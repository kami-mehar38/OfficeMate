package com.krtechnologies.officemate

import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText


class SignUpActivity : AppCompatActivity() {

    // properties
    private var etName: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etName = findViewById<EditText>(R.id.etName)

        etName?.setOnFocusChangeListener { view, b -> if (b) changeToAccent(view as EditText) else changeToPrimary(view as EditText) }
    }


    // changes the color filter of drawable to accent color
    private fun changeToAccent(editText: EditText) {
        editText.compoundDrawables?.get(0)?.setColorFilter(resources.getColor(R.color.colorAccentDark), PorterDuff.Mode.SRC_ATOP)
    }

    // changes the color filter of drawable to primary color
    private fun changeToPrimary(editText: EditText) {
        editText.compoundDrawables?.get(0)?.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
    }
}
