package com.krtechnologies.officemate.helpers

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Patterns

/**
 * This project is created by Kamran Ramzan on 23-Aug-18.
 */
class Validator {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var validator: Validator? = null

        @Synchronized
        fun getInstance(): Validator {
            synchronized(Helper::class) {
                if (validator == null)
                    validator = Validator()
                return validator!!
            }
        }
    }

    fun validateName(name: String): Boolean {
        return (!TextUtils.isEmpty(name) && name.length >= 6)
    }

    fun validateEmail(email: String): Boolean {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    fun validatePassword(password: String): Boolean {
        return (!TextUtils.isEmpty(password) && password.length >= 6)
    }

    fun validateText(text: String): Boolean {
        return !TextUtils.isEmpty(text)
    }

    fun validateTitle(name: String): Boolean {
        return (!TextUtils.isEmpty(name) && name.length >= 6)
    }
}