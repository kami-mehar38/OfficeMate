package com.krtechnologies.officemate

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.krtechnologies.officemate.fragments.NewsFeedFragment
import com.krtechnologies.officemate.fragments.WorkstationFragment
import com.krtechnologies.officemate.helpers.Helper
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private var currentIndex: Int = 0
    private var previousIndex: Int = 0
    private var newsFeedFragment: NewsFeedFragment? = null
    private var workstationFragment: WorkstationFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // initializing the views
        initViews()
        initFragment()

        // loading the first menu from bottom navigation when application starts for the first time
        if (savedInstanceState == null) {
            selectBottomNavigationItem()
        }
    }

    private fun initFragment() {
        newsFeedFragment = NewsFeedFragment()
        workstationFragment = WorkstationFragment()
    }

    private fun initViews() {
        menuNewsfeed.setOnClickListener {
            currentIndex = 0
            selectBottomNavigationItem()
        }

        menuWorkstation.setOnClickListener {
            currentIndex = 1
            selectBottomNavigationItem()
        }

        menuMembers.setOnClickListener {
            currentIndex = 2
            selectBottomNavigationItem()
        }

        menuProfile.setOnClickListener {
            currentIndex = 3
            selectBottomNavigationItem()
        }
    }

    private fun selectBottomNavigationItem() {
        when (currentIndex) {
            0 -> {
                Helper.getInstance().changeToRed(menuNewsfeed.compoundDrawables[1])
                menuNewsfeed.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                loadFragment()
            }

            1 -> {
                Helper.getInstance().changeToRed(menuWorkstation.compoundDrawables[1])
                menuWorkstation.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                loadFragment()
            }

            2 -> {
                Helper.getInstance().changeToRed(menuMembers.compoundDrawables[1])
                menuMembers.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
            }

            3 -> {
                Helper.getInstance().changeToRed(menuProfile.compoundDrawables[1])
                menuProfile.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
            }
        }

        if (currentIndex != previousIndex) {
            when (previousIndex) {
                0 -> {
                    Helper.getInstance().changeToPrimary(menuNewsfeed.compoundDrawables[1])
                    menuNewsfeed.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                }

                1 -> {
                    Helper.getInstance().changeToPrimary(menuWorkstation.compoundDrawables[1])
                    menuWorkstation.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                }

                2 -> {
                    Helper.getInstance().changeToPrimary(menuMembers.compoundDrawables[1])
                    menuMembers.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                }

                3 -> {
                    Helper.getInstance().changeToPrimary(menuProfile.compoundDrawables[1])
                    menuProfile.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                }
            }
        }
        previousIndex = currentIndex

    }

    private fun loadFragment() {

        Handler().post {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)

            val fragment = getFragment()

            if (fragment.isAdded) {
                fragmentTransaction.show(fragment)
            } else {
                fragmentTransaction.add(R.id.frame, fragment, fragment.tag)
            }

            when (currentIndex) {
                0 -> {
                    if (workstationFragment?.isVisible!!)
                        fragmentTransaction.hide(workstationFragment)
                }
                1 -> {
                    if (newsFeedFragment?.isVisible!!)
                        fragmentTransaction.hide(newsFeedFragment)
                }
            }

            fragmentTransaction.commit()
        }
    }

    private fun getFragment(): Fragment = when (currentIndex) {
        0 -> newsFeedFragment as Fragment
        1 -> workstationFragment as Fragment
        else -> newsFeedFragment as Fragment
    }

}
