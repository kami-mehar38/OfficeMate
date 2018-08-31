package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
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
 * Created by ingizly on 8/16/18
 **/

class WorkstationProjectsViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private var data: MutableLiveData<MutableList<Project>>? = null
    private var list = ArrayList<Project>()
    private var previousList = ArrayList<Project>()

    init {
        loadData()
    }

    private fun loadData() {
        if (data == null) {
            data = MutableLiveData()
            loadDataFromServer()
        }
    }

    fun getData(): MutableLiveData<MutableList<Project>> {
        return data!!
    }

    fun updateData(workstationProjectList: MutableList<Project>) {
        data?.value = workstationProjectList
    }

    fun loadDataFromServer() {
        list.clear()
        AndroidNetworking.get("${Helper.BASE_URL}/project/${PreferencesManager.getInstance().getUserAdminEmail()}/${PreferencesManager.getInstance().getUserEmail()}")
                .setTag("projects")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        info { response }
                        when (response?.getInt("status")) {
                            201 -> {
                                if (response.has("projects")) {
                                    val jsonArray = response.getJSONArray("projects")
                                    for (employeeAt in 0 until jsonArray.length()) {
                                        val project = Helper.getInstance().getGson().fromJson(jsonArray.getJSONObject(employeeAt).toString(), Project::class.java)
                                        list.add(project)
                                    }
                                    data?.value = list
                                    previousList.addAll(list)
                                }
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        data?.value = previousList
                    }

                })
    }
}