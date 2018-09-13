package com.krtechnologies.officemate.helpers

import android.support.v7.util.DiffUtil
import com.krtechnologies.officemate.models.File

/**
 * This project is created by Kamran Ramzan on 13-Sep-18.
 */
class FilesDiffUtils(private val newList: List<File>, private val oldList: List<File>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            when (oldList[oldItemPosition].compareTo(newList[newItemPosition])) {
                0 -> true
                else -> false
            }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return true
    }
}