package com.krtechnologies.officemate.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.adapters.NewsFeedAdapter
import com.krtechnologies.officemate.models.NewsFeedViewModel
import com.krtechnologies.officemate.models.Project
import kotlinx.android.synthetic.main.fragment_news_feed.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.Serializable


class NewsFeedFragment : Fragment(), Serializable, AnkoLogger {

    private var newsFeedAdapter: NewsFeedAdapter? = null
    private var listProjects: MutableList<Project>? = null
    private var newListProjects: MutableList<Project>? = null
    private var newsFeedViewModel: NewsFeedViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            newsFeedAdapter = NewsFeedAdapter(it)
        }
        listProjects = ArrayList()
        newListProjects = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_feed, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvNewsFeed.layoutManager = LinearLayoutManager(context)
        rvNewsFeed.hasFixedSize()

        newsFeedAdapter?.let {
            rvNewsFeed.adapter = it
        }

        swipeRefreshLayout.setOnRefreshListener {
            newsFeedViewModel?.loadDataFromServer()
        }

        swipeRefreshLayout.isRefreshing = true

        newsFeedViewModel = ViewModelProviders.of(this).get(NewsFeedViewModel::class.java)
        newsFeedViewModel?.getData()?.observe(this, Observer<MutableList<Project>> {
            swipeRefreshLayout.isRefreshing = false
            if (!it!!.isEmpty()) {
                if (rvNewsFeed.visibility != View.VISIBLE)
                    rvNewsFeed.visibility = View.VISIBLE
                if (tvNoNewsFeed.visibility != View.GONE)
                    tvNoNewsFeed.visibility = View.GONE
                newsFeedAdapter?.updateList(it)
                rvNewsFeed?.smoothScrollToPosition(0)
                listProjects = it
            } else {
                if (rvNewsFeed.visibility != View.GONE)
                    rvNewsFeed.visibility = View.GONE
                if (tvNoNewsFeed.visibility != View.VISIBLE)
                    tvNoNewsFeed.visibility = View.VISIBLE

            }
        })

    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            resetData()
        }
    }

    fun filterNewsFeed(searchText: String) {

        if (searchText.isNotEmpty()) {
            listProjects?.let {
                it.forEach { project: Project ->
                    if (project.assignedTo.contains(searchText, true)) {
                        newListProjects?.add(project)
                        info { true }
                    }
                }
            }
            newsFeedViewModel?.updateData(newListProjects!!)
        } else newsFeedViewModel?.updateData(listProjects!!)

        newListProjects?.clear()
    }

    fun resetData() {
        newsFeedViewModel?.updateData(listProjects!!)
    }
}
