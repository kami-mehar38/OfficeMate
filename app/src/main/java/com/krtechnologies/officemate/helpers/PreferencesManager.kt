package com.krtechnologies.officemate.helpers

import android.content.Context
import android.content.SharedPreferences

/**
 * This project is created by Kamran Ramzan on 20-Aug-18.
 */

class PreferencesManager {

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
}