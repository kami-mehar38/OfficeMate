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
import com.krtechnologies.officemate.adapters.WorkstationsProjectAdapter
import com.krtechnologies.officemate.models.NewsFeed
import com.krtechnologies.officemate.models.NewsFeedViewModel
import com.krtechnologies.officemate.models.WorkstationProject
import com.krtechnologies.officemate.models.WorkstationProjectsViewModel
import kotlinx.android.synthetic.main.fragment_workstation.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.Serializable


class WorkstationFragment : Fragment(), Serializable, AnkoLogger {

    private var workstationsProjectAdapter: WorkstationsProjectAdapter? = null
    private var listWorkstationProject: MutableList<WorkstationProject>? = null
    private var newListWorkstationProject: MutableList<WorkstationProject>? = null
    private var workstationProjectsViewModel: WorkstationProjectsViewModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            workstationsProjectAdapter = WorkstationsProjectAdapter(it)
        }
        listWorkstationProject = ArrayList()
        newListWorkstationProject = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workstation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvWorkstation.layoutManager = LinearLayoutManager(context)
        rvWorkstation.hasFixedSize()

        workstationsProjectAdapter?.let {
            rvWorkstation.adapter = it
        }

        workstationProjectsViewModel = ViewModelProviders.of(this).get(WorkstationProjectsViewModel::class.java)
        workstationProjectsViewModel?.getData()?.observe(this, Observer<MutableList<WorkstationProject>> {
            if (!it!!.isEmpty()) {
                if (rvWorkstation.visibility != View.VISIBLE)
                    rvWorkstation.visibility = View.VISIBLE
                if (tvNoProjects.visibility != View.GONE)
                    tvNoProjects.visibility = View.GONE
                workstationsProjectAdapter?.updateList(it)
                rvWorkstation?.smoothScrollToPosition(0)
            } else {
                if (rvWorkstation.visibility != View.GONE)
                    rvWorkstation.visibility = View.GONE
                if (tvNoProjects.visibility != View.VISIBLE)
                    tvNoProjects.visibility = View.VISIBLE

            }
        })

        listWorkstationProject = workstationProjectsViewModel?.getData()?.value
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            resetData()
        }
    }

    fun filterWorkstationProject(searchText: String) {

        if (searchText.isNotEmpty()) {
            listWorkstationProject?.let {
                it.forEach { workstationProject: WorkstationProject ->
                    if (workstationProject.projectName.contains(searchText, true)) {
                        newListWorkstationProject?.add(workstationProject)
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
