package com.krtechnologies.officemate

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
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
import com.krtechnologies.officemate.adapters.TasksAdapter
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.SimpleDividerItemDecoration
import com.krtechnologies.officemate.helpers.TaskItemDivider
import com.krtechnologies.officemate.models.Task
import com.krtechnologies.officemate.models.TasksViewModel
import kotlinx.android.synthetic.main.activity_tasks.*
import org.jetbrains.anko.*

class TasksActivity : AppCompatActivity(), AnkoLogger {

    private var isSearchExpanded = false
    private val REQUEST_CODE_ADD_TASK = 1

    private var isFirstLoad = true
    private var tasksAdapter: TasksAdapter? = null
    private var listTasks: MutableList<Task>? = null
    private var newListTasks: MutableList<Task>? = null
    private var tasksViewModel: TasksViewModel? = null
    private var task: Task? = null

    override fun onStart() {
        super.onStart()
        listTasks = ArrayList()
        newListTasks = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        //setting the support action bar
        setSupportActionBar(toolbar)

        initViews()
    }

    private fun initViews() {

        tasksAdapter = TasksAdapter(this)
        tasksAdapter?.run {
            setItemClickListener {
                info { it.toString() }
                task = it
            }
        }

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.addItemDecoration(TaskItemDivider(this))
        rvTasks.hasFixedSize()

        tasksAdapter?.let {
            rvTasks.adapter = it
        }

        swipeRefreshLayout.setOnRefreshListener {
            tasksViewModel?.loadDataFromServer()
        }

        swipeRefreshLayout.isRefreshing = true
        tasksViewModel = ViewModelProviders.of(this).get(TasksViewModel::class.java)
        tasksViewModel?.getData()?.observe(this, Observer<MutableList<Task>> {
            swipeRefreshLayout.isRefreshing = false
            if (!it!!.isEmpty()) {
                if (rvTasks.visibility != View.VISIBLE)
                    rvTasks.visibility = View.VISIBLE
                if (tvNoTasks.visibility != View.GONE)
                    tvNoTasks.visibility = View.GONE
                tasksAdapter?.updateList(it)
                rvTasks?.smoothScrollToPosition(0)
                info { it }
            } else {
                if (rvTasks.visibility != View.GONE)
                    rvTasks.visibility = View.GONE
                if (tvNoTasks.visibility != View.VISIBLE)
                    tvNoTasks.visibility = View.VISIBLE
            }

            if (isFirstLoad) {
                listTasks = it
                isFirstLoad = false
            }
        })


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
        menuInflater.inflate(R.menu.menu_tasks_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item!!.itemId) {
            R.id.action_search -> {
                showSearchEditText()
                true
            }
            R.id.action_add -> {
                startActivityForResult<TaskAddingActivity>(REQUEST_CODE_ADD_TASK)
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
                val animView = ViewAnimationUtils.createCircularReveal(searchContainer, searchContainer.right - ((searchContainer.right / 2) / 2), searchContainer.top + (searchContainer.height / 2), 0f, endRadius.toFloat())
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
                val animView = ViewAnimationUtils.createCircularReveal(searchContainer, searchContainer.right - ((searchContainer.right / 2) / 2), searchContainer.top + (searchContainer.height / 2), startRadius.toFloat(), 0f)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == Activity.RESULT_OK) {
            data?.let { data ->
                val task = data.getSerializableExtra(TaskAddingActivity.EXTRA_TASK) as Task
                toast(task.toString())
                info { tasksAdapter?.getList()?.forEach { info { it.toString() } } }
            }
        }
    }
}
