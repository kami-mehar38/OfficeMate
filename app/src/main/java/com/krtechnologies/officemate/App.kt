package com.krtechnologies.officemate

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager

/**
 * This project is created by Kamran Ramzan on 10-Aug-18.
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        fun getAppContext(): Context = context!!
    }

    override fun onCreate() {
        super.onCreate()

        // initializing the Preferences Manager
        PreferencesManager.getInstance().init(applicationContext)

        // initializing the helper
        Helper.getInstance().init(applicationContext)

        // initializing the networking library
        AndroidNetworking.initialize(applicationContext)

        context = applicationContext
    }
}