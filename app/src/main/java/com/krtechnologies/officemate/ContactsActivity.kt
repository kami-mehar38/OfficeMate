package com.krtechnologies.officemate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.solver.widgets.Helper
import android.support.v7.widget.LinearLayoutManager
import com.krtechnologies.officemate.adapters.ContactsAdapter
import kotlinx.android.synthetic.main.activity_contacts.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class ContactsActivity : AppCompatActivity() {

    private lateinit var contactsAdapter: ContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        contactsAdapter = ContactsAdapter(this)
        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.adapter = contactsAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadContacts()
        }
        loadContacts()
    }

    private fun loadContacts() {
        doAsync {
            swipeRefreshLayout.isRefreshing = true
            val list = com.krtechnologies.officemate.helpers.Helper.getInstance().getContacts()
            uiThread {
                swipeRefreshLayout.isRefreshing = false
                contactsAdapter.updateList(list)
            }
        }
    }
}
