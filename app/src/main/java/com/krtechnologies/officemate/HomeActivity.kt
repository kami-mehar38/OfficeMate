package com.krtechnologies.officemate

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.krtechnologies.officemate.helpers.Helper
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private var currentIndex: Int = 0
    private var previousIndex: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // initializing the views
        initViews()

        // loading the first menu from bottom navigation when application starts for the first time
        if (savedInstanceState == null) {
            selectBottomNavigationItem()
        }
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
            }

            1 -> {
                Helper.getInstance().changeToRed(menuWorkstation.compoundDrawables[1])
                menuWorkstation.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
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
}
