package com.krtechnologies.officemate.adapters

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
import com.krtechnologies.officemate.helpers.TasksDiffUtils
import com.krtechnologies.officemate.helpers.WorkstationProjectsDiffUtils
import com.krtechnologies.officemate.models.Project
import com.krtechnologies.officemate.models.Task
import org.jetbrains.anko.AnkoLogger

/**
 * This project is created by Kamran Ramzan on 20-Sep-18.
 */
class TasksAdapter(val context: Context) : RecyclerView.Adapter<TasksAdapter.ViewHolder>(), AnkoLogger {

    private var tasksList: MutableList<Task> = ArrayList()
    private var listener: ((Task) -> Unit)? = null

    fun setItemClickListener(listener: (Task) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_task, parent, false))


    override fun getItemCount(): Int {
        return tasksList.size
    }

    fun getList(): MutableList<Task> = tasksList

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = tasksList[position]
        with(project) {
            holder.tvTitle.text = title
            holder.tvDescription.text = description
            holder.tvTimeStamp.text = timestamp
        }

        holder.item.setOnClickListener { _ ->
            listener?.let {
                it(project)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            /*val project = tasksList[position]
            payloads.forEach {
                it as Bundle
                if (it.containsKey("project_description")) {
                    holder.tvProjectDescription.text = it["project_description"].toString()
                }
                if (it.containsKey("completion")) {
                    holder.tvCompletion.text = context.getString(R.string.completed) + it["completion"].toString()
                }
                if (it.containsKey("eta")) {
                    holder.tvEta.text = context.getString(R.string.eta) + it["eta"].toString()
                }
            }

            holder.workstationProject.setOnClickListener { _ ->
                listener?.let {
                    it(project)
                }
            }*/
        }
    }

    fun updateList(newList: MutableList<Task>) {
        val diffResult = DiffUtil.calculateDiff(TasksDiffUtils(newList, this.tasksList), true)
        diffResult.dispatchUpdatesTo(this)
        this.tasksList.clear()
        this.tasksList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)!!
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)!!
        val tvTimeStamp = view.findViewById<TextView>(R.id.tvTimeStamp)!!
        val item = view.findViewById<ConstraintLayout>(R.id.item)!!
    }
}