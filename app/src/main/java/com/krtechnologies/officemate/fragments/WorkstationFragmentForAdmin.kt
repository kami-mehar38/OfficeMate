package com.krtechnologies.officemate.fragments


import android.app.Activity
import android.arch.lifecycle.extensions.R.id.info
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krtechnologies.officemate.MemberSelectingActivity
import com.krtechnologies.officemate.R
import kotlinx.android.synthetic.main.fragment_workstation_fragment_for_admin.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class WorkstationFragmentForAdmin : Fragment(), AnkoLogger {

    companion object {
        val REQUEST_CODE_MEMBER_SELECT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workstation_fragment_for_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAssignTo.setOnClickListener {
            startActivityForResult(Intent(context, MemberSelectingActivity::class.java), REQUEST_CODE_MEMBER_SELECT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WorkstationFragmentForAdmin.REQUEST_CODE_MEMBER_SELECT && resultCode == Activity.RESULT_OK) {
            data?.let {
                info { it.getSerializableExtra(MemberSelectingActivity.EXTRA_EMPLOYEE) }
            }

        }
    }

}
