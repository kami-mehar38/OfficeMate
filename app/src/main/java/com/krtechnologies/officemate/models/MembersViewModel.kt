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
            data?.value = ArrayList<Member>().apply {
                add(Member("0", "Kamran Ramzan", "Software Engineer"))
                add(Member("1", "Shaban Arshad", "Software Engineer"))
                add(Member("2", "Zeeshan", "Software Engineer"))
                add(Member("3", "Nadeem Arshad", "Software Engineer"))
                add(Member("4", "Tufail Shah", "Software Engineer"))
                add(Member("5", "Tipu Khan", "Software Engineer"))
                add(Member("6", "Anees Aslam", "Software Engineer"))
                add(Member("7", "Mahed ALi", "Software Engineer"))
                add(Member("8", "Kamran Ramzan", "Software Engineer"))
                add(Member("9", "Kamran Ramzan", "Software Engineer"))
            }
        }
    }

    fun getData(): MutableLiveData<MutableList<Member>> {
        return data!!
    }

    fun updateData(newsFeedList: MutableList<Member>) {
        data?.value = newsFeedList
    }
}