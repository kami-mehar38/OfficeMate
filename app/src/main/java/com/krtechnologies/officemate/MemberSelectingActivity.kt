package com.krtechnologies.officemate

import android.app.Activity
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.krtechnologies.officemate.adapters.MembersAdapter
import com.krtechnologies.officemate.helpers.SimpleDividerItemDecoration
import com.krtechnologies.officemate.models.Employee
import com.krtechnologies.officemate.models.MembersViewModel
import kotlinx.android.synthetic.main.activity_member_selecting.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import com.androidnetworking.error.ANError
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.krtechnologies.officemate.helpers.Helper
import es.dmoral.toasty.Toasty


class MemberSelectingActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        const val EXTRA_EMPLOYEE = "EMPLOYEE"
    }

    private var isFirstLoad = true
    private var membersAdapter: MembersAdapter? = null
    private var listEmployees: MutableList<Employee>? = null
    private var newListEmployees: MutableList<Employee>? = null
    private var membersViewModel: MembersViewModel? = null
    private var inputMethodManager: InputMethodManager? = null
    private var employee: Employee? = null

    override fun onStart() {
        super.onStart()
        listEmployees = ArrayList()
        newListEmployees = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_selecting)
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        membersAdapter = MembersAdapter(this, true)
        membersAdapter?.run {
            setItemClickListener {
                info { it.toString() }
                employee = it
            }
        }

        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.addItemDecoration(SimpleDividerItemDecoration(this))
        rvMembers.hasFixedSize()

        membersAdapter?.let {
            rvMembers.adapter = it
        }

        swipeRefreshLayout.setOnRefreshListener {
            membersViewModel?.loadDataFromServer()
        }

        swipeRefreshLayout.isRefreshing = true
        membersViewModel = ViewModelProviders.of(this).get(MembersViewModel::class.java)
        membersViewModel?.getData()?.observe(this, Observer<MutableList<Employee>> {
            swipeRefreshLayout.isRefreshing = false
            if (!it!!.isEmpty()) {
                if (rvMembers.visibility != View.VISIBLE)
                    rvMembers.visibility = View.VISIBLE
                if (tvNoMembers.visibility != View.GONE)
                    tvNoMembers.visibility = View.GONE
                membersAdapter?.updateList(it)
                rvMembers?.smoothScrollToPosition(0)
                info { it }
            } else {
                if (rvMembers.visibility != View.GONE)
                    rvMembers.visibility = View.GONE
                if (tvNoMembers.visibility != View.VISIBLE)
                    tvNoMembers.visibility = View.VISIBLE
            }

            if (isFirstLoad) {
                listEmployees = it
                isFirstLoad = false
            }
        })

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                filterMembers(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        ivCancel.setOnClickListener {
            etSearch.text.clear()
            showKeyboard()
        }

        btnDone.setOnClickListener {
            val returnIntent = Intent()

            returnIntent.putExtra(EXTRA_EMPLOYEE, employee)
            if (employee != null)
                setResult(Activity.RESULT_OK, returnIntent)
            else setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }
    }

    fun filterMembers(searchText: String) {

        if (searchText.isNotEmpty()) {
            listEmployees?.let {
                it.forEach { employee: Employee ->
                    if (employee.name.contains(searchText, true)) {
                        newListEmployees?.add(employee)
                    }
                }
            }
            membersViewModel?.updateData(newListEmployees!!)
        } else membersViewModel?.updateData(listEmployees!!)

        newListEmployees?.clear()
    }

    fun resetData() {
        listEmployees?.run {
            membersViewModel?.updateData(this)
        }
    }

    private fun showKeyboard() {
        if (etSearch.requestFocus())
            inputMethodManager?.showSoftInputFromInputMethod(etSearch.windowToken, InputMethodManager.SHOW_IMPLICIT)
    }

}
