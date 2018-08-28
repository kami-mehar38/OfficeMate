package com.krtechnologies.officemate.helpers

import android.content.Context
import android.content.SharedPreferences
import com.krtechnologies.officemate.models.Admin
import com.krtechnologies.officemate.models.Employee

/**
 * This project is created by Kamran Ramzan on 20-Aug-18.
 */

class PreferencesManager {

    // KEYS
    private val KEY_ID = "ID"
    private val KEY_NAME = "NAME"
    private val KEY_PROFILE_PICTURE = "PROFILE_PICTURE"
    private val KEY_EMAIL = "EMAIL"
    private val KEY_EMAIL_ADMIN = "EMAIL_ADMIN"
    private val KEY_PASSWORD = "PASSWORD"
    private val KEY_ORGANIZATION = "ORGANIZATION"
    private val KEY_DESIGNATION = "DESIGNATION"
    private val KEY_JOINING_DATE = "JOINING_DATE"
    private val KEY_SUBSCRIPTION = "SUBSCRIPTION"
    private val KEY_IS_ADMIN = "IS_ADMIN"
    private val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"

    private var sharedPreferences: SharedPreferences? = null

    companion object {
        private var INSTANCE: PreferencesManager? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): PreferencesManager {
            synchronized(PreferencesManager::class) {
                if (INSTANCE == null)
                    INSTANCE = PreferencesManager()
                return INSTANCE!!
            }
        }
    }

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("com.krtechnologies.officemate", Context.MODE_PRIVATE)
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences!!.edit()
    }

    fun saveUser(any: Any) {
        if (any is Admin) {
            with(any) {
                getEditor().run {
                    putString(KEY_ID, id)
                    putString(KEY_NAME, name)
                    putString(KEY_PROFILE_PICTURE, profilePicture)
                    putString(KEY_EMAIL, email)
                    putString(KEY_PASSWORD, password)
                    putString(KEY_ORGANIZATION, organization)
                    putString(KEY_DESIGNATION, designation)
                    putString(KEY_JOINING_DATE, joiningDate)
                    putString(KEY_SUBSCRIPTION, subscription)
                    putString(KEY_IS_ADMIN, isAdmin)
                    apply()
                }
            }
        } else if (any is Employee) {
            with(any) {
                getEditor().run {
                    putString(KEY_ID, id)
                    putString(KEY_NAME, name)
                    putString(KEY_PROFILE_PICTURE, profilePicture)
                    putString(KEY_EMAIL, email)
                    putString(KEY_PASSWORD, password)
                    putString(KEY_ORGANIZATION, organization)
                    putString(KEY_DESIGNATION, designation)
                    putString(KEY_JOINING_DATE, joiningDate)
                    putString(KEY_SUBSCRIPTION, subscription)
                    putString(KEY_IS_ADMIN, isAdmin)
                    putString(KEY_EMAIL_ADMIN, adminEmail)
                    apply()
                }
            }
        }
    }

    fun setLogInStatus(isLoggedIn: Boolean) {
        getEditor().run {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        sharedPreferences!!.run {
            return getBoolean(KEY_IS_LOGGED_IN, false)
        }
    }

    fun getUserName(): String {
        sharedPreferences!!.run {
            return getString(KEY_NAME, "")
        }
    }

    fun getUserDesignation(): String {
        sharedPreferences!!.run {
            return getString(KEY_DESIGNATION, "")
        }
    }

    fun getIsAdmin(): String {
        sharedPreferences!!.run {
            return getString(KEY_IS_ADMIN, "")
        }
    }

    fun getProfilePicture(): String {
        sharedPreferences!!.run {
            return getString(KEY_PROFILE_PICTURE, "")
        }
    }
}