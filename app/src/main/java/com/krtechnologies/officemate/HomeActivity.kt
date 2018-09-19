package com.krtechnologies.officemate

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.krtechnologies.officemate.fragments.*
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import com.krtechnologies.officemate.models.Employee
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doFromSdk
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity


class HomeActivity : AppCompatActivity(), AnkoLogger {

    private var currentIndex: Int = 0
    private var previousIndex: Int = 0

    //fragments
    private var newsFeedFragment: NewsFeedFragment? = null
    private var workstationFragment: WorkstationFragment? = null
    private var workstationFragmentForAdmin: WorkstationFragmentForAdmin? = null
    private var membersFragment: MembersFragment? = null
    private var settingsFragment: SettingsFragment? = null

    private var isSearchExpanded = false

    // keys
    private val KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION = "CURRENT_INDEX"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setting the support action bar
        setSupportActionBar(toolbar)

        // initializing the views
        initViews()

        savedInstanceState?.let {
            if (it.containsKey(KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION)) {
                currentIndex = it.getInt(KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION, 0)
                selectBottomNavigationItem()
            }
        }

        // loading the first menu from bottom navigation when application starts for the first time
        if (savedInstanceState == null) {
            initFragment()
            selectBottomNavigationItem()
        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.run {
            putInt(KEY_CURRENT_INDEX_OF_BOTTOM_NAVIGATION, currentIndex)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.run {
            when (currentIndex) {
                3 -> findItem(R.id.action_search).isVisible = false
                1 -> if (PreferencesManager.getInstance().getIsAdmin()) findItem(R.id.action_search).isVisible = false
                else -> findItem(R.id.action_search).isVisible = true
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item!!.itemId) {
            R.id.action_search -> {
                showSearchEditText()
                true
            }
            R.id.action_tasks -> {
                startActivity<TasksActivity>()
                true
            }
            else -> false
        }
    }

    private fun initFragment() {
        newsFeedFragment = NewsFeedFragment()
        workstationFragment = WorkstationFragment()
        workstationFragmentForAdmin = WorkstationFragmentForAdmin()
        membersFragment = MembersFragment()
        settingsFragment = SettingsFragment()
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

        menuSettings.setOnClickListener {
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
                when (currentIndex) {
                    0 -> newsFeedFragment?.filterNewsFeed(p0.toString())
                    1 -> workstationFragment?.filterWorkstationProject(p0.toString())
                    2 -> membersFragment?.filterMembers(p0.toString())
                }
            }
        })
        etSearch.setOnEditorActionListener { editText, action, _ ->
            when (action) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    Helper.getInstance().hideKeyboard(editText)
                    true
                }
                else -> false
            }
        }
    }

    private fun selectBottomNavigationItem() {
        when (currentIndex) {
            0 -> {
                Helper.getInstance().changeRed(menuNewsfeed.compoundDrawables[1])
                menuNewsfeed.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                tvTitle.text = resources.getString(R.string.news_feed)
                loadFragment()
                ivBack.performClick()
            }

            1 -> {
                Helper.getInstance().changeRed(menuWorkstation.compoundDrawables[1])
                menuWorkstation.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                tvTitle.text = resources.getString(R.string.workstation)
                loadFragment()
                ivBack.performClick()
            }

            2 -> {
                Helper.getInstance().changeRed(menuMembers.compoundDrawables[1])
                menuMembers.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                tvTitle.text = resources.getString(R.string.members)
                loadFragment()
                ivBack.performClick()
            }

            3 -> {
                Helper.getInstance().changeRed(menuSettings.compoundDrawables[1])
                menuSettings.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                tvTitle.text = resources.getString(R.string.settings)
                loadFragment()
                ivBack.performClick()
            }
        }

        if (currentIndex != previousIndex) {
            when (previousIndex) {
                0 -> {
                    Helper.getInstance().changeToBlack(menuNewsfeed.compoundDrawables[1])
                    menuNewsfeed.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                }

                1 -> {
                    Helper.getInstance().changeToBlack(menuWorkstation.compoundDrawables[1])
                    menuWorkstation.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                }

                2 -> {
                    Helper.getInstance().changeToBlack(menuMembers.compoundDrawables[1])
                    menuMembers.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                }

                3 -> {
                    Helper.getInstance().changeToBlack(menuSettings.compoundDrawables[1])
                    menuSettings.setTextColor(ContextCompat.getColor(this, android.R.color.black))
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

            if (supportFragmentManager.findFragmentByTag(fragment.tag) == null) {

                if (fragment.isAdded) {
                    fragmentTransaction.show(fragment)
                } else {
                    fragmentTransaction.add(R.id.frame, fragment, fragment.tag)
                }

                when (currentIndex) {
                    0 -> {
                        if (workstationFragment?.isVisible!!)
                            fragmentTransaction.hide(workstationFragment)
                        if (workstationFragmentForAdmin?.isVisible!!)
                            fragmentTransaction.hide(workstationFragmentForAdmin)
                        if (membersFragment?.isVisible!!)
                            fragmentTransaction.hide(membersFragment)
                        if (settingsFragment?.isVisible!!)
                            fragmentTransaction.hide(settingsFragment)
                    }
                    1 -> {
                        if (newsFeedFragment?.isVisible!!)
                            fragmentTransaction.hide(newsFeedFragment)
                        if (membersFragment?.isVisible!!)
                            fragmentTransaction.hide(membersFragment)
                        if (settingsFragment?.isVisible!!)
                            fragmentTransaction.hide(settingsFragment)
                    }
                    2 -> {
                        if (workstationFragment?.isVisible!!)
                            fragmentTransaction.hide(workstationFragment)
                        if (workstationFragmentForAdmin?.isVisible!!)
                            fragmentTransaction.hide(workstationFragmentForAdmin)
                        if (newsFeedFragment?.isVisible!!)
                            fragmentTransaction.hide(newsFeedFragment)
                        if (settingsFragment?.isVisible!!)
                            fragmentTransaction.hide(settingsFragment)
                    }
                    3 -> {
                        if (workstationFragment?.isVisible!!)
                            fragmentTransaction.hide(workstationFragment)
                        if (workstationFragmentForAdmin?.isVisible!!)
                            fragmentTransaction.hide(workstationFragmentForAdmin)
                        if (newsFeedFragment?.isVisible!!)
                            fragmentTransaction.hide(newsFeedFragment)
                        if (membersFragment?.isVisible!!)
                            fragmentTransaction.hide(membersFragment)
                    }
                }
                fragmentTransaction.commit()
                invalidateOptionsMenu()
            }
        }
    }

    private fun getFragment(): Fragment = when (currentIndex) {
        0 -> newsFeedFragment as Fragment
        1 -> {
            if (PreferencesManager.getInstance().getIsAdmin())
                workstationFragmentForAdmin as Fragment
            else workstationFragment as Fragment
        }
        2 -> membersFragment as Fragment
        3 -> settingsFragment as Fragment
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
                        Helper.getInstance().showKeyboard()

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
                        when (currentIndex) {
                            0 -> newsFeedFragment?.resetData()
                            1 -> workstationFragment?.resetData()
                            2 -> membersFragment?.resetData()
                        }
                        Helper.getInstance().hideKeyboard(etSearch)
                        etSearch.text.clear()
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
