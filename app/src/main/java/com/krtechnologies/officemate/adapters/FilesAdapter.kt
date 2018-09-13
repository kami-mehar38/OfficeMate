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
import com.krtechnologies.officemate.helpers.ContactsDiffUtils
import com.krtechnologies.officemate.helpers.FilesDiffUtils
import com.krtechnologies.officemate.models.Contact
import com.krtechnologies.officemate.models.File

/**
 * This project is created by Kamran Ramzan on 13-Sep-18.
 */
class FilesAdapter(val context: Context) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private var filesList: MutableList<File> = ArrayList()
    private lateinit var listener: ((file: File) -> Unit)

    public fun setOnItemClickListener(listener: (file: File) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_file, parent, false))


    override fun getItemCount(): Int {
        return filesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val file = filesList[position]
        holder.tvName.text = file.fileName
        holder.tvFileSize.text = file.fileSize
        holder.item.setOnClickListener {
            listener(file)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
        }
    }

    fun updateList(newList: List<File>) {
        val diffResult = DiffUtil.calculateDiff(FilesDiffUtils(newList, this.filesList), true)
        diffResult.dispatchUpdatesTo(this)
        this.filesList.clear()
        this.filesList.addAll(newList)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvName)!!
        val tvFileSize = view.findViewById<TextView>(R.id.tvSize)!!
        val item = view.findViewById<ConstraintLayout>(R.id.item)!!
    }
}