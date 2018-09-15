package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.NewsFeedDiffUtils
import com.krtechnologies.officemate.models.Project

/**
 * Created by ingizly on 9/15/18
 **/
class ProjectAdapter(val context: Context) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {

    private var projectsList: MutableList<Project> = ArrayList()
    private lateinit var listener: ((project: Project) -> Unit)

    fun setOnItemClickListener(listener: (project: Project) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_project, parent, false))


    override fun getItemCount(): Int {
        return projectsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val project = projectsList[position]
        holder.tvAssignedTo.text = project.assignedTo
        holder.tvProjectName.text = project.projectName
        holder.item.setOnClickListener { _ ->
            listener(project)
        }

        Glide.with(context)
                .asBitmap()
                .load(project.profilePicture)
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(50f).toInt(), Helper.getInstance().convertDpToPixel(50f).toInt()).fallback(R.drawable.person).error(R.drawable.person))
                .into(object : Target<Bitmap> {
                    override fun onLoadStarted(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                    }

                    override fun getSize(cb: SizeReadyCallback) {
                    }

                    override fun getRequest(): Request? {
                        return null
                    }

                    override fun onStop() {
                    }

                    override fun setRequest(request: Request?) {
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onStart() {
                    }

                    override fun onDestroy() {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        holder.ivProfilePicture.setImageBitmap(resource)
                    }

                })

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
        }
    }

    fun updateList(newList: MutableList<Project>) {
        val diffResult = DiffUtil.calculateDiff(NewsFeedDiffUtils(newList, this.projectsList), true)
        diffResult.dispatchUpdatesTo(this)
        this.projectsList.clear()
        this.projectsList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivProfilePicture = view.findViewById<ImageView>(R.id.ivProfilePicture)!!
        val tvAssignedTo = view.findViewById<TextView>(R.id.tvAssignedTo)!!
        val tvProjectName = view.findViewById<TextView>(R.id.tvProjectName)!!
        val item = view.findViewById<ConstraintLayout>(R.id.item)!!
    }
}