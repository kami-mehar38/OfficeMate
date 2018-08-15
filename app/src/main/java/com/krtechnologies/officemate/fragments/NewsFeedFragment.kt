package com.krtechnologies.officemate.fragments

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
import kotlinx.android.synthetic.main.fragment_news_feed.*


class NewsFeedFragment : Fragment() {

    private var newsFeedAdapter: NewsFeedAdapter? = null
    private var listNewsFeed: MutableList<NewsFeed>? = null
    private var newListNewsFeed: MutableList<NewsFeed>? = null


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

        listNewsFeed?.apply {
            add(NewsFeed("0", "Kamran Ramzan"))
            add(NewsFeed("1", "Kamran Ramzan"))
            add(NewsFeed("2", "Kamran Ramzan"))
            add(NewsFeed("3", "Bhai"))
        }

        newsFeedAdapter?.updateList(listNewsFeed!!)
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
                    if (newsFeed.name.contains(searchText, true))
                        newListNewsFeed?.add(newsFeed)
                }
            }
            newsFeedAdapter?.updateList(newListNewsFeed!!)
        } else newsFeedAdapter?.updateList(listNewsFeed!!)

        newListNewsFeed?.clear()
    }

}
