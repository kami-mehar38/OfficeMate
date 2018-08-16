package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Created by ingizly on 8/16/18
 **/

class WorkstationProjectsViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private var data: MutableLiveData<MutableList<WorkstationProject>>? = null

    init {
        loadData()
    }


    private fun loadData() {
        if (data == null) {
            data = MutableLiveData()
            data?.value = ArrayList<WorkstationProject>().apply {
                add(WorkstationProject("1", "Ingizly", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("2", "App Locker", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("3", "Cricket", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("4", "Campus App", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("5", "Camcorder", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("6", "Facebook", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("7", "Play Store", "I have been working on this project for some time... and it will be finished within days"))
                add(WorkstationProject("8", "App Store", "I have been working on this project for some time... and it will be finished within days"))
            }
        }
    }

    fun getData(): MutableLiveData<MutableList<WorkstationProject>> {
        return data!!
    }

    fun updateData(workstationProjectList: MutableList<WorkstationProject>) {
        data?.value = workstationProjectList
    }
}