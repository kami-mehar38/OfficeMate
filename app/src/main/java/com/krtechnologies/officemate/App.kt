package com.krtechnologies.officemate

import android.app.Application
import com.androidnetworking.AndroidNetworking
import com.krtechnologies.officemate.helpers.Helper

/**
 * This project is created by Kamran Ramzan on 10-Aug-18.
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        // initializing the helper
        Helper.getInstance().init(applicationContext)

        // initializing the Android Networking Library
        AndroidNetworking.initialize(applicationContext)
    }
}