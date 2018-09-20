package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.json.JSONObject

/**
 * Created by ingizly on 9/19/18
 **/
class TasksViewModel(application: Application) : AnkoLogger, AndroidViewModel(application) {

    private var data: MutableLiveData<MutableList<Task>>? = null
    private var list = ArrayList<Task>()
    private var previousList = ArrayList<Task>()


    init {
        loadData()
    }

    private fun loadData() {
        if (data == null) {
            data = MutableLiveData()
            loadDataFromServer()
        }
    }

    fun getData(): MutableLiveData<MutableList<Task>> {
        return data!!
    }

    fun updateData(workstationProjectList: MutableList<Task>) {
        data?.value = workstationProjectList
    }

    fun loadDataFromServer() {
        list.clear()

        AndroidNetworking.get("${Helper.BASE_URL}/task/${PreferencesManager.getInstance().getUserEmail()}")
                .setTag("projects")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        info { response }
                        when (response?.getInt("status")) {
                            201 -> {
                                if (response.has("tasks")) {
                                    val jsonArray = response.getJSONArray("tasks")
                                    for (employeeAt in 0 until jsonArray.length()) {
                                        val task = Helper.getInstance().getGson().fromJson(jsonArray.getJSONObject(employeeAt).toString(), Task::class.java)
                                        list.add(task)
                                    }
                                    data?.value = list
                                    previousList.addAll(list)
                                }
                            }
                            202 -> data?.value = list
                        }
                    }

                    override fun onError(anError: ANError?) {
                        data?.value = previousList
                    }

                })
    }
}