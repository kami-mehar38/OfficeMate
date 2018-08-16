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
import com.krtechnologies.officemate.models.NewsFeed
import com.krtechnologies.officemate.models.NewsFeedViewModel
import kotlinx.android.synthetic.main.fragment_news_feed.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class NewsFeedFragment : Fragment(), AnkoLogger {

    private var newsFeedAdapter: NewsFeedAdapter? = null
    private var listNewsFeed: MutableList<NewsFeed>? = null
    private var newListNewsFeed: MutableList<NewsFeed>? = null
    private var newsFeedViewModel: NewsFeedViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            newsFeedAdapter = NewsFeedAdapter(it)
        }
        listNewsFeed = ArrayList()
        newListNewsFeed = ArrayList()
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

        newsFeedViewModel = ViewModelProviders.of(this).get(NewsFeedViewModel::class.java)
        newsFeedViewModel?.getData()?.observe(this, Observer<MutableList<NewsFeed>> {
            newsFeedAdapter?.updateList(it!!)
            rvNewsFeed?.smoothScrollToPosition(0)
        })

        listNewsFeed = newsFeedViewModel?.getData()?.value

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment News.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                NewsFeedFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }


    fun filterNewsFeed(searchText: String) {

        if (searchText.isNotEmpty()) {
            listNewsFeed?.let {
                it.forEach { newsFeed: NewsFeed ->
                    if (newsFeed.name.contains(searchText, true)) {
                        newListNewsFeed?.add(newsFeed)
                        info { true }
                    }
                }
            }
            newsFeedViewModel?.updateData(newListNewsFeed!!)
        } else newsFeedViewModel?.updateData(listNewsFeed!!)

        newListNewsFeed?.clear()
    }

    fun resetData() {
        newsFeedViewModel?.updateData(listNewsFeed!!)
    }
}
