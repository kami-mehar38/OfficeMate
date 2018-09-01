package com.krtechnologies.officemate.adapters

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.WorkstationProjectEditActivity
import com.krtechnologies.officemate.fragments.WorkstationFragment
import com.krtechnologies.officemate.helpers.WorkstationProjectsDiffUtils
import com.krtechnologies.officemate.models.Project
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivityForResult

/**
 * Created by ingizly on 8/15/18
 **/

class WorkstationsProjectAdapter(val context: Context) : RecyclerView.Adapter<WorkstationsProjectAdapter.ViewHolder>(), AnkoLogger {

    private var workstationProjectList: MutableList<Project> = ArrayList()
    private var listener: ((Project) -> Unit)? = null

    fun setItemClickListener(listener: (Project) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_workstation_projects, parent, false))


    override fun getItemCount(): Int {
        return workstationProjectList.size
    }

    fun getList(): MutableList<Project> = workstationProjectList

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = workstationProjectList[position]
        holder.tvProjectName.text = project.projectName
        holder.tvProjectDescription.text = project.projectDescription
        holder.tvCompletion.text = context.getString(R.string.completed) + " ${project.completion}"
        holder.tvEta.text = context.getString(R.string.eta) + " ${project.eta}"

        holder.workstationProject.setOnClickListener { _ ->
            listener?.let {
                it(project)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            val project = workstationProjectList[position]
            payloads.forEach {
                it as Bundle
                if (it.containsKey("project_description")) {
                    holder.tvProjectDescription.text = it["project_description"].toString()
                }
                if (it.containsKey("completion")) {
                    holder.tvCompletion.text = it["completion"].toString()
                }
                if (it.containsKey("eta")) {
                    holder.tvEta.text = it["eta"].toString()
                }
            }

            holder.workstationProject.setOnClickListener { _ ->
                listener?.let {
                    it(project)
                }
            }
        }
    }

    fun updateList(newList: MutableList<Project>) {
        val diffResult = DiffUtil.calculateDiff(WorkstationProjectsDiffUtils(newList, this.workstationProjectList), true)
        diffResult.dispatchUpdatesTo(this)
        this.workstationProjectList.clear()
        this.workstationProjectList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName = view.findViewById<TextView>(R.id.tvProjectName)!!
        val tvProjectDescription = view.findViewById<TextView>(R.id.tvProjectDescription)!!
        val tvEta = view.findViewById<TextView>(R.id.tvEta)!!
        val tvCompletion = view.findViewById<TextView>(R.id.tvCompletion)!!
        val workstationProject = view.findViewById<ConstraintLayout>(R.id.workstationProject)!!
    }
}