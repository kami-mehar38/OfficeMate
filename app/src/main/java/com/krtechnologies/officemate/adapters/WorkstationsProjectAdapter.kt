package com.krtechnologies.officemate.adapters

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.WorkstationProjectsDiffUtils
import com.krtechnologies.officemate.models.WorkstationProject

/**
 * Created by ingizly on 8/15/18
 **/

class WorkstationsProjectAdapter(val context: Context) : RecyclerView.Adapter<WorkstationsProjectAdapter.ViewHolder>() {

    private var workstationProjectList: MutableList<WorkstationProject> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_workstation_projects, parent, false))


    override fun getItemCount(): Int {
        return workstationProjectList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
        }

    }

    fun updateList(newList: MutableList<WorkstationProject>) {
        val diffResult = DiffUtil.calculateDiff(WorkstationProjectsDiffUtils(newList, this.workstationProjectList), true)
        diffResult.dispatchUpdatesTo(this)
        this.workstationProjectList.clear()
        this.workstationProjectList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}