package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread

/**
 * Created by ingizly on 8/16/18
 **/

class NewsFeedViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private var data: MutableLiveData<MutableList<NewsFeed>>? = null

    init {
        loadData()
    }


    private fun loadData() {
        if (data == null) {
            data = MutableLiveData()
            data?.value = ArrayList<NewsFeed>().apply {
                add(NewsFeed("0", "Kamran Ramzan"))
                add(NewsFeed("1", "Kamran Ramzan"))
                add(NewsFeed("2", "Kamran Ramzan"))
                add(NewsFeed("3", "Bhai"))
            }

        }
    }

    fun getData(): MutableLiveData<MutableList<NewsFeed>> {
        return data!!
    }

    fun updateData(newsFeedList: MutableList<NewsFeed>) {
        info { newsFeedList.toString() }
        data?.value = newsFeedList
    }
}