package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.krtechnologies.officemate.HomeActivity
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.util.*

/**
 * This project is created by Kamran Ramzan on 17-Aug-18.
 */

class MembersViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private var data: MutableLiveData<MutableList<Employee>>? = null
    val list = ArrayList<Employee>()
    private var previousList = ArrayList<Employee>()

    init {
        loadData()
    }

    private fun loadData() {
        if (data == null) {
            data = MutableLiveData()

            loadDataFromServer()
        }
    }

    fun getData(): MutableLiveData<MutableList<Employee>> {
        return data!!
    }

    fun updateData(newsFeedList: MutableList<Employee>) {
        data?.value = newsFeedList
    }

    fun loadDataFromServer() {
        list.clear()
        val adminEmail = when (PreferencesManager.getInstance().getIsAdmin()) {
            "0" -> PreferencesManager.getInstance().getUserAdminEmail()
            else -> PreferencesManager.getInstance().getUserEmail()
        }

        AndroidNetworking.get("${Helper.BASE_URL}/members/$adminEmail/${PreferencesManager.getInstance().getUserEmail()}")
                .setTag("login")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        info { response }
                        when (response?.getInt("status")) {
                            201 -> {
                                if (response.has("employees")) {
                                    val jsonArray = response.getJSONArray("employees")
                                    for (employeeAt in 0 until jsonArray.length()) {
                                        val employee = Helper.getInstance().getGson().fromJson(jsonArray.getJSONObject(employeeAt).toString(), Employee::class.java)
                                        list.add(employee)
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