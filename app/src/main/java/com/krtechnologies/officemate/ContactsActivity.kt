package com.krtechnologies.officemate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import com.krtechnologies.officemate.adapters.ContactsAdapter
import com.krtechnologies.officemate.helpers.ContactItemDivider
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.models.Contact
import kotlinx.android.synthetic.main.activity_contacts.*
import org.jetbrains.anko.*

class ContactsActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        const val EXTRA_CONTACT = "EXTRA_CONTACT"
    }

    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var list: ArrayList<Contact>
    private lateinit var newList: ArrayList<Contact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        contactsAdapter = ContactsAdapter(this)
        contactsAdapter.setOnItemClickListener { contact ->
            alert("Send this contact?") {
                yesButton {
                    sendResultBack(contact)
                }
                cancelButton { }
            }.show()
        }

        rvContacts.layoutManager = LinearLayoutManager(this)
        val itemDecoration = ContactItemDivider(this)
        rvContacts.addItemDecoration(itemDecoration)
        rvContacts.adapter = contactsAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadContacts()
        }
        loadContacts()

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivCancel.setOnClickListener {
            etSearch.text.clear()
            Helper.getInstance().showKeyboard()
        }

        newList = ArrayList()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let { text ->
                    if (text.isNotEmpty()) {
                        newList.clear()
                        list.forEach {
                            if (it.name.contains(text, true)) {
                                newList.add(it)
                                info { it.toString() }
                            }
                        }
                        contactsAdapter.updateList(newList)
                    } else contactsAdapter.updateList(list)
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

    private fun sendResultBack(contact: Contact) {
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_CONTACT, contact)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun loadContacts() {
        doAsync {
            swipeRefreshLayout.isRefreshing = true
            list = Helper.getInstance().getContacts()
            uiThread {
                swipeRefreshLayout.isRefreshing = false
                contactsAdapter.updateList(list)
            }
        }
    }
}
