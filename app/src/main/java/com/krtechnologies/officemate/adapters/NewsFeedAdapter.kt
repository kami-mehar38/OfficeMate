package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.NewsFeedDiffUtils
import com.krtechnologies.officemate.models.NewsFeed
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.TextView
import com.krtechnologies.officemate.fragments.NewsFeedFragment


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

        val spannableString = SpannableString("Kamran Ramzan")

        holder.tvName.setText(spannableString, TextView.BufferType.SPANNABLE)
        val spannableText = holder.tvName.text as Spannable
        spannableText.setSpan(ForegroundColorSpan(Color.BLUE), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


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
        val tvName = view.findViewById<TextView>(R.id.tvName)!!
    }
}