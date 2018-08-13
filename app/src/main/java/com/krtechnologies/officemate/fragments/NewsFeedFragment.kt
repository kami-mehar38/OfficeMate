package com.krtechnologies.officemate.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.R
import android.R.attr.endColor
import android.R.attr.startColor
import com.ldoublem.ringPregressLibrary.Ring
import kotlinx.android.synthetic.main.fragment_news_feed.*


class NewsFeedFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_feed, container, false)
    }

    private var mCounter: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val r1 = Ring(50, "Hi there", "50%", startColor, endColor)
        val r2 = Ring(50, "Hi there", "50%", startColor, endColor)
        val r3 = Ring(50, "Hi there", "50%", startColor, endColor)
        val r4 = Ring(50, "Hi there", "50%", startColor, endColor)

        val listRing = ArrayList<Ring>()
        listRing.add(r1)
        listRing.add(r2)
        listRing.add(r3)
        listRing.add(r4)
        ring_progress.setData(listRing, 1500)

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
}
