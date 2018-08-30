package com.krtechnologies.officemate.helpers

import android.support.v7.util.DiffUtil
import com.krtechnologies.officemate.models.Project

/**
 * This project is created by Kamran Ramzan on 13-Aug-18.
 */

class NewsFeedDiffUtils(private val newList: MutableList<Project>, private val oldList: MutableList<Project>) : DiffUtil.Callback() {


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = when (oldList[oldItemPosition].compareTo(newList[newItemPosition])) {
        0 -> true
        else -> false
    }


    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return true
    }
}