package com.krtechnologies.officemate.helpers

import android.support.v7.util.DiffUtil
import com.krtechnologies.officemate.models.Message
import com.krtechnologies.officemate.models.Project

/**
 * Created by ingizly on 9/1/18
 **/

class MessagesDiffUtils(private val newList: MutableList<Message>, private val oldList: MutableList<Message>) : DiffUtil.Callback() {


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