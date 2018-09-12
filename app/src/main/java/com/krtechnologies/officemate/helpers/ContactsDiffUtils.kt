package com.krtechnologies.officemate.helpers

import android.support.v7.util.DiffUtil
import com.krtechnologies.officemate.models.Contact

/**
 * Created by ingizly on 9/12/18
 **/
class ContactsDiffUtils(private val newList: List<Contact>, private val oldList: List<Contact>) : DiffUtil.Callback() {


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