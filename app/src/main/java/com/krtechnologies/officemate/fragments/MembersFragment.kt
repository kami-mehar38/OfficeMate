package com.krtechnologies.officemate.fragments


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.adapters.MembersAdapter
import com.krtechnologies.officemate.helpers.SimpleDividerItemDecoration
import com.krtechnologies.officemate.models.Employee
import com.krtechnologies.officemate.models.Member
import com.krtechnologies.officemate.models.MembersViewModel
import kotlinx.android.synthetic.main.fragment_members.*
import java.io.Serializable

class MembersFragment : Fragment(), Serializable {

    private var isFirstLoad = true
    private var membersAdapter: MembersAdapter? = null
    private var listEmployees: MutableList<Employee>? = null
    private var newListEmployees: MutableList<Employee>? = null
    private var membersViewModel: MembersViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            membersAdapter = MembersAdapter(it, false)
        }
        listEmployees = ArrayList()
        newListEmployees = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvMembers.layoutManager = LinearLayoutManager(context)
        rvMembers.addItemDecoration(SimpleDividerItemDecoration(context!!))
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
                if (isFirstLoad) {
                    listEmployees = it
                    isFirstLoad = false
                }
            } else {
                if (rvMembers.visibility != View.GONE)
                    rvMembers.visibility = View.GONE
                if (tvNoMembers.visibility != View.VISIBLE)
                    tvNoMembers.visibility = View.VISIBLE
            }


        })

    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            resetData()
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
}
