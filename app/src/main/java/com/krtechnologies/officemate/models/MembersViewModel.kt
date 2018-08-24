package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * This project is created by Kamran Ramzan on 17-Aug-18.
 */

class MembersViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private var data: MutableLiveData<MutableList<Member>>? = null

    init {
        loadData()
    }

    private fun loadData() {
        if (data == null) {
            data = MutableLiveData()
            data?.value = ArrayList<Member>()
        }
    }

    fun getData(): MutableLiveData<MutableList<Member>> {
        return data!!
    }

    fun updateData(newsFeedList: MutableList<Member>) {
        data?.value = newsFeedList
    }
}