package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
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
 * This project is created by Kamran Ramzan on 13-Aug-18.
 */

class NewsFeedAdapter(val context: Context) : RecyclerView.Adapter<NewsFeedAdapter.ViewHolder>() {

    private var projectsList: MutableList<Project> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_news_feed, parent, false))


    override fun getItemCount(): Int {
        return projectsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val project = projectsList[position]
        holder.progressBar.setProgress(50.0, 100.0)
        holder.progressBar.setProgressTextAdapter { progress -> "$progress%" }

        holder.tvName.text = project.assignedTo

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
        val progressBar = view.findViewById<CircularProgressIndicator>(R.id.ProgressBar)!!
        val ivProfilePicture = view.findViewById<ImageView>(R.id.ivProfilePicture)!!
        val tvName = view.findViewById<TextView>(R.id.tvName)!!

    }
}