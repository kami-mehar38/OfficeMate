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
            data?.value = ArrayList<WorkstationProject>()
        }
    }

    fun getData(): MutableLiveData<MutableList<WorkstationProject>> {
        return data!!
    }

    fun updateData(workstationProjectList: MutableList<WorkstationProject>) {
        data?.value = workstationProjectList
    }
}