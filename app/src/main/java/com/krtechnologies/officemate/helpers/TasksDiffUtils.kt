package com.krtechnologies.officemate.helpers

import android.os.Bundle
import android.support.v7.util.DiffUtil
import com.krtechnologies.officemate.models.Project
import com.krtechnologies.officemate.models.Task

/**
 * This project is created by Kamran Ramzan on 20-Sep-18.
 */
class TasksDiffUtils(private val newList: MutableList<Task>, private val oldList: MutableList<Task>) : DiffUtil.Callback() {


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = when (oldList[oldItemPosition].compareTo(newList[newItemPosition])) {
        0 -> true
        else -> false
    }


    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTask = oldList[oldItemPosition]
        val newTask = newList[newItemPosition]
        return true
    }

    /*override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldProject = oldList[oldItemPosition]
        val newProject = newList[newItemPosition]

        val bundle = Bundle()
        if (oldProject.projectDescription != newProject.projectDescription)
            bundle.putString("project_description", newProject.projectDescription)
        if (oldProject.completion != newProject.completion)
            bundle.putString("completion", newProject.completion)
        if (oldProject.eta != newProject.eta)
            bundle.putString("eta", newProject.eta)
        return bundle
    }*/
}