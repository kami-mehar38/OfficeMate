package com.krtechnologies.officemate.adapters

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.WorkstationProjectEditActivity
import com.krtechnologies.officemate.helpers.WorkstationProjectsDiffUtils
import com.krtechnologies.officemate.models.Project
import com.krtechnologies.officemate.models.WorkstationProject
import org.jetbrains.anko.startActivity

/**
 * Created by ingizly on 8/15/18
 **/

class WorkstationsProjectAdapter(val context: Context) : RecyclerView.Adapter<WorkstationsProjectAdapter.ViewHolder>() {

    private var workstationProjectList: MutableList<Project> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_workstation_projects, parent, false))


    override fun getItemCount(): Int {
        return workstationProjectList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = workstationProjectList[position]
        holder.tvProjectName.text = project.projectName
        holder.tvProjectDescription.text = project.projectDescription
        holder.tvCompletion.text = context.getString(R.string.completed) + " ${project.completion}"
        holder.tvEta.text = context.getString(R.string.eta) + " ${project.eta}"

        holder.workstationProject.setOnClickListener {
            context.startActivity<WorkstationProjectEditActivity>(WorkstationProjectEditActivity.KEY_EXTRA_PROJECT to project)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
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