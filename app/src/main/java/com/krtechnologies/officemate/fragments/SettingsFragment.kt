package com.krtechnologies.officemate.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.ProfileSettingsActivity
import com.krtechnologies.officemate.R
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.Serializable


class SettingsFragment : Fragment(), Serializable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ivProfilePicture.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, ivProfilePicture as View, "ivProfilePicture")
            val intent = Intent(context!!, ProfileSettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent, options.toBundle())
        }
    }
}
