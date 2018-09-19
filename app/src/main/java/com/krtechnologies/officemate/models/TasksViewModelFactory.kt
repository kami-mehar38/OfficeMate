package com.krtechnologies.officemate.models

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

/**
 * Created by ingizly on 9/19/18
 **/
class TasksViewModelFactory (private val userEmail: String, private val adminEmial: String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkstationProjectsViewModel(userEmail, adminEmial) as T
    }
}