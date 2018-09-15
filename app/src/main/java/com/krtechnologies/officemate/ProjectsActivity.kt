package com.krtechnologies.officemate

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import com.krtechnologies.officemate.adapters.WorkstationsProjectAdapter
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.models.Project
import com.krtechnologies.officemate.models.ViewModelFactory
import com.krtechnologies.officemate.models.WorkstationProjectsViewModel
import kotlinx.android.synthetic.main.activity_projects.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doFromSdk
import org.jetbrains.anko.info

class ProjectsActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        const val REQUEST_CODE_EDIT_PROJECT = 4
        const val KEY_EXTRA_PROJECT = "EXTRA_PROJECT"
    }

    private var workstationsProjectAdapter: WorkstationsProjectAdapter? = null
    private var listWorkstationProject: MutableList<Project>? = null
    private var newListWorkstationProject: MutableList<Project>? = null
    private var workstationProjectsViewModel: WorkstationProjectsViewModel? = null
    private var project: Project? = null
    private var projectUpdated: Project? = null

    private var isSearchExpanded = false
    private var isFirstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        project = intent?.run {
            getSerializableExtra(KEY_EXTRA_PROJECT) as Project
        }

        //setting the support action bar
        setSupportActionBar(toolbar)

        project?.run {
            tvNoProjects.text = assignedTo
        }


        workstationsProjectAdapter = WorkstationsProjectAdapter(this)

        listWorkstationProject = ArrayList()
        newListWorkstationProject = ArrayList()

        rvProjects.layoutManager = LinearLayoutManager(this)
        rvProjects.hasFixedSize()

        workstationsProjectAdapter?.let {
            rvProjects.adapter = it
        }

        workstationsProjectAdapter?.setItemClickListener {
            startActivityForResult(Intent(this, WorkstationProjectEditActivity::class.java).putExtra(KEY_EXTRA_PROJECT, it), REQUEST_CODE_EDIT_PROJECT)
            info { it }
        }

        swipeRefreshLayout.setOnRefreshListener {
            workstationProjectsViewModel?.loadDataFromServer()
        }

        swipeRefreshLayout.isRefreshing = true

        project?.let { project ->
            workstationProjectsViewModel = ViewModelProviders.of(this, ViewModelFactory(project.email)).get(WorkstationProjectsViewModel::class.java)
            workstationProjectsViewModel?.getData()?.observe(this, Observer<MutableList<Project>> {
                swipeRefreshLayout.isRefreshing = false
                if (!it!!.isEmpty()) {
                    if (rvProjects.visibility != View.VISIBLE)
                        rvProjects.visibility = View.VISIBLE
                    if (tvNoProjects.visibility != View.GONE)
                        tvNoProjects.visibility = View.GONE
                    workstationsProjectAdapter?.updateList(it)
                    rvProjects?.smoothScrollToPosition(0)
                    if (isFirstTime) {
                        listWorkstationProject = it
                        isFirstTime = !isFirstTime
                    }
                } else {
                    if (rvProjects.visibility != View.GONE)
                        rvProjects.visibility = View.GONE
                    if (tvNoProjects.visibility != View.VISIBLE)
                        tvNoProjects.visibility = View.VISIBLE

                }
            })
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
                filterProjects(p0?.toString()!!)
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

    fun filterProjects(searchText: String) {

        if (searchText.isNotEmpty()) {
            listWorkstationProject?.let {
                it.forEach { project: Project ->
                    if (project.projectName.contains(searchText, true)) {
                        newListWorkstationProject?.add(project)
                        info { true }
                    }
                }
            }
            workstationProjectsViewModel?.updateData(newListWorkstationProject!!)
        } else workstationProjectsViewModel?.updateData(listWorkstationProject!!)

        newListWorkstationProject?.clear()
    }

    fun resetData() {
        workstationProjectsViewModel?.updateData(listWorkstationProject!!)
    }
}
