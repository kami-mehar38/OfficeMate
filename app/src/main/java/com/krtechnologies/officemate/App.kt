package com.krtechnologies.officemate

import android.app.Application
import com.androidnetworking.AndroidNetworking
import com.google.firebase.FirebaseApp
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager

/**
 * This project is created by Kamran Ramzan on 10-Aug-18.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // initializing the Preferences Manager
        PreferencesManager.getInstance().init(applicationContext)

        // initializing the helper
        Helper.getInstance().init(applicationContext)

        // initializing the networking library
        AndroidNetworking.initialize(applicationContext)

        // initializing the Firebase App
        FirebaseApp.initializeApp(applicationContext)
    }
}