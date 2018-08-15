package com.krtechnologies.officemate.helpers

import android.support.v7.util.DiffUtil
import com.krtechnologies.officemate.models.WorkstationProject

/**
 * Created by ingizly on 8/15/18
 **/

class WorkstationProjectsDiffUtils(private val newList: MutableList<WorkstationProject>, private val oldList: MutableList<WorkstationProject>) : DiffUtil.Callback() {


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