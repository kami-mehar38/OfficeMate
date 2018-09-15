package com.krtechnologies.officemate.models

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

/**
 * Created by ingizly on 9/15/18
 **/
class ViewModelFactory(private val userEmail: String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkstationProjectsViewModel(userEmail) as T
    }
}