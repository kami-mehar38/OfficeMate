package com.krtechnologies.officemate.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.krtechnologies.officemate.R

/**
 * Created by ingizly on 8/6/18
 **/

class FragmentIntroductorySlides : Fragment() {

    var fragmentNumber: Int? = null

    companion object {
        private const val EXTRA_FRAGMENT_NUMBER: String = "FRAGMENT_NUMBER"

        fun getInstance(fragmentNumber: Int): FragmentIntroductorySlides {
            val fragmentIntroductorySlides = FragmentIntroductorySlides()
            val bundle = Bundle()
            bundle.putInt(EXTRA_FRAGMENT_NUMBER, fragmentNumber)
            fragmentIntroductorySlides.arguments = bundle
            return fragmentIntroductorySlides
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            fragmentNumber = arguments?.getInt(EXTRA_FRAGMENT_NUMBER, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_introductory_slides, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvNumber = view.findViewById<TextView>(R.id.tvNumber)
        tvNumber.text = fragmentNumber?.toString() ?: ""
    }

}