package com.krtechnologies.officemate

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import com.krtechnologies.officemate.fragments.NewsFeedFragment
import com.krtechnologies.officemate.fragments.WorkstationFragment
import com.krtechnologies.officemate.helpers.Helper
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.doFromSdk


class HomeActivity : AppCompatActivity() {

    private var currentIndex: Int = 0
    private var previousIndex: Int = 0
    private var newsFeedFragment: NewsFeedFragment? = null
    private var workstationFragment: WorkstationFragment? = null
    private var isSearchExpanded = false
    private var inputMethodManager: InputMethodManager? = null

    private val KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION: String = "CURRENT_INDEX"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setting the support action bar
        setSupportActionBar(toolbar)

        // initializing the views
        initViews()
        initFragment()

        savedInstanceState?.let {
            if (it.containsKey(KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION)) {
                currentIndex = it.getInt(KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION, 0)
                selectBottomNavigationItem()
            }
        }

        // loading the first menu from bottom navigation when application starts for the first time
        if (savedInstanceState == null) {
            selectBottomNavigationItem()
        }

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION, currentIndex)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item!!.itemId) {
            R.id.action_search -> {
                showSearchEditText()
                true
            }
            else -> false
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

        ivBack.setOnClickListener {
            if (isSearchExpanded)
                hideSearchEditText()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (currentIndex == 0)
                    newsFeedFragment?.filterNewsFeed(p0.toString())
            }

        })
    }

    private fun selectBottomNavigationItem() {
        when (currentIndex) {
            0 -> {
                Helper.getInstance().changeToSecondary(menuNewsfeed.compoundDrawables[1])
                menuNewsfeed.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary))
                tvTitle.text = resources.getString(R.string.news_feed)
                loadFragment()
                ivBack.performClick()
            }

            1 -> {
                Helper.getInstance().changeToSecondary(menuWorkstation.compoundDrawables[1])
                menuWorkstation.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary))
                tvTitle.text = resources.getString(R.string.workstation)
                loadFragment()
                ivBack.performClick()
            }

            2 -> {
                Helper.getInstance().changeToSecondary(menuMembers.compoundDrawables[1])
                menuMembers.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary))
                tvTitle.text = resources.getString(R.string.members)
                ivBack.performClick()
            }

            3 -> {
                Helper.getInstance().changeToSecondary(menuProfile.compoundDrawables[1])
                menuProfile.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary))
                tvTitle.text = resources.getString(R.string.profile)
                ivBack.performClick()
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

    @SuppressLint("NewApi")
    private fun showSearchEditText() {

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            searchContainer.postDelayed({
                val endRadius = Math.hypot(searchContainer.width.toDouble(), searchContainer.height.toDouble()).toInt()
                val animView = ViewAnimationUtils.createCircularReveal(searchContainer, searchContainer.right - ((searchContainer.right / 2) / 6), searchContainer.top + (searchContainer.height / 2), 0f, endRadius.toFloat())
                animView.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (toolbar.visibility != View.INVISIBLE)
                            toolbar.visibility = View.INVISIBLE
                        etSearch.requestFocus()
                        inputMethodManager?.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)

                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        if (searchContainer.visibility != View.VISIBLE)
                            searchContainer.visibility = View.VISIBLE
                        changeStatusBarColorToBlack()
                        isSearchExpanded = true
                    }

                })
                animView.duration = 300
                animView.interpolator = AccelerateDecelerateInterpolator()
                animView.start()
            }, 1)

        }
    }

    @SuppressLint("NewApi")
    private fun hideSearchEditText() {

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            searchContainer.postDelayed({
                val startRadius = Math.hypot(searchContainer.width.toDouble(), searchContainer.height.toDouble()).toInt()
                val animView = ViewAnimationUtils.createCircularReveal(searchContainer, searchContainer.right - ((searchContainer.right / 2) / 6), searchContainer.top + (searchContainer.height / 2), startRadius.toFloat(), 0f)
                animView.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (searchContainer.visibility != View.INVISIBLE)
                            searchContainer.visibility = View.INVISIBLE
                        isSearchExpanded = false
                        inputMethodManager?.hideSoftInputFromWindow(etSearch.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        changeStatusBarColorToPrimaryDark()
                        if (toolbar.visibility != View.VISIBLE)
                        toolbar.visibility = View.VISIBLE
                    }

                })
                animView.duration = 300
                animView.interpolator = AccelerateDecelerateInterpolator()
                animView.start()

            }, 1)
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun changeStatusBarColorToBlack() {
        window.statusBarColor = Color.BLACK
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun changeStatusBarColorToPrimaryDark() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    override fun onBackPressed() {
        if (isSearchExpanded)
            hideSearchEditText()
        else
            super.onBackPressed()
    }
}
