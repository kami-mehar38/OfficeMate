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
import com.krtechnologies.officemate.models.Member
import com.krtechnologies.officemate.models.MembersViewModel
import kotlinx.android.synthetic.main.fragment_members.*
import java.io.Serializable

class MembersFragment : Fragment(), Serializable {

    private var membersAdapter: MembersAdapter? = null
    private var listMembers: MutableList<Member>? = null
    private var newListMembers: MutableList<Member>? = null
    private var membersViewModel: MembersViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            membersAdapter = MembersAdapter(it)
        }
        listMembers = ArrayList()
        newListMembers = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvMembers.layoutManager = LinearLayoutManager(context)
        rvMembers.hasFixedSize()

        membersAdapter?.let {
            rvMembers.adapter = it
        }

        membersViewModel = ViewModelProviders.of(this).get(MembersViewModel::class.java)
        membersViewModel?.getData()?.observe(this, Observer<MutableList<Member>> {
            membersAdapter?.updateList(it!!)
            rvMembers?.smoothScrollToPosition(0)
        })

        listMembers = membersViewModel?.getData()?.value
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            resetData()
        }
    }

    fun filterMembers(searchText: String) {

        if (searchText.isNotEmpty()) {
            listMembers?.let {
                it.forEach { member: Member ->
                    if (member.name.contains(searchText, true)) {
                        newListMembers?.add(member)
                    }
                }
            }
            membersViewModel?.updateData(newListMembers!!)
        } else membersViewModel?.updateData(listMembers!!)

        newListMembers?.clear()
    }

    fun resetData() {
        membersViewModel?.updateData(listMembers!!)
    }
}
