package com.krtechnologies.officemate.adapters

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.R
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
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)

    }

    fun updateList(newList: MutableList<NewsFeed>) {
        val diffResult = DiffUtil.calculateDiff(NewsFeedDiffUtils(newList, this.newsFeedList), true)
        diffResult.dispatchUpdatesTo(this)
        this.newsFeedList.clear()
        this.newsFeedList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}