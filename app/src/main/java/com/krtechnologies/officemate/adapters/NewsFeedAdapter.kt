package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.NewsFeedDiffUtils
import com.krtechnologies.officemate.models.NewsFeed


/**
 * This project is created by Kamran Ramzan on 13-Aug-18.
 */

class NewsFeedAdapter(val context: Context) : RecyclerView.Adapter<NewsFeedAdapter.ViewHolder>() {

    private var newsFeedList: MutableList<NewsFeed> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_news_feed, parent, false))


    override fun getItemCount(): Int {
        return newsFeedList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.progressBar.setProgress(50.0, 100.0)
        holder.progressBar.setProgressTextAdapter { progress -> "$progress%" }

        val spannableString = SpannableString(newsFeedList[position].name)
        holder.tvName.setText(spannableString, TextView.BufferType.SPANNABLE)

        Glide.with(context)
                .asBitmap()
                .load(R.drawable.kamran)
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(50f).toInt(), Helper.getInstance().convertDpToPixel(50f).toInt()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).fallback(R.drawable.person).error(R.drawable.person))
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

    fun updateList(newList: MutableList<NewsFeed>) {
        val diffResult = DiffUtil.calculateDiff(NewsFeedDiffUtils(newList, this.newsFeedList), true)
        diffResult.dispatchUpdatesTo(this)
        this.newsFeedList.clear()
        this.newsFeedList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val progressBar = view.findViewById<CircularProgressIndicator>(R.id.ProgressBar)!!
        val ivProfilePicture = view.findViewById<ImageView>(R.id.ivProfilePicture)!!
        val tvName = view.findViewById<TextView>(R.id.tvName)!!

        init {
            tvName.setSpannableFactory(Helper.getInstance().getSpannableFactory())
        }
    }
}