package com.krtechnologies.officemate

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.krtechnologies.officemate.adapters.MessagesAdapter
import com.krtechnologies.officemate.models.Message
import com.krtechnologies.officemate.models.WorkstationProjectsViewModel
import kotlinx.android.synthetic.main.activity_messaging.*
import org.jetbrains.anko.doFromSdk

class MessagingActivity : AppCompatActivity() {

    private var messagesAdapter: MessagesAdapter? = null
    private var listMessages: MutableList<Message>? = null
    private var newListMessages: MutableList<Message>? = null
    private var inputMethodManager: InputMethodManager? = null
    private var isSearchExpanded = false


    override fun onStart() {
        super.onStart()
        listMessages = ArrayList()
        newListMessages = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        //setting the support action bar
        setSupportActionBar(toolbar)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        messagesAdapter = MessagesAdapter(this)

        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.hasFixedSize()

        messagesAdapter?.let {
            rvMessages.adapter = it
        }

        messagesAdapter?.let {
            it.updateList(ArrayList<Message>().apply {
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
                add(Message(1, 1, ""))
                add(Message(1, 0, ""))
            })
        }

        ivBack.setOnClickListener {
            if (isSearchExpanded)
                hideSearchEditText()
        }

        etSearch.setOnEditorActionListener { _, action, _ ->
            when (action) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    hideKeyboard()
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
                        showKeyboard()

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
                        hideKeyboard()
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

    fun showKeyboard() {
        inputMethodManager?.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard() {
        inputMethodManager?.hideSoftInputFromWindow(etSearch.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
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
