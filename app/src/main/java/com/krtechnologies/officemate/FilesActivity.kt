package com.krtechnologies.officemate

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import com.krtechnologies.officemate.adapters.ContactsAdapter
import com.krtechnologies.officemate.adapters.FilesAdapter
import com.krtechnologies.officemate.helpers.ContactItemDivider
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.models.File
import kotlinx.android.synthetic.main.activity_files.*
import org.jetbrains.anko.*

class FilesActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        const val EXTRA_FILE = "EXTRA_FILE"
    }

    private lateinit var filesAdapter: FilesAdapter
    private lateinit var list: ArrayList<File>
    private lateinit var newList: ArrayList<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        filesAdapter = FilesAdapter(this)
        filesAdapter.setOnItemClickListener { file ->
            alert("Send this file?") {
                yesButton {
                    sendResultBack(file)
                }
                cancelButton { }
            }.show()
        }

        rvFiles.layoutManager = LinearLayoutManager(this)
        val itemDecoration = ContactItemDivider(this)
        rvFiles.addItemDecoration(itemDecoration)
        rvFiles.adapter = filesAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadFiles()
        }
        loadFiles()

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
                            if (it.fileName.contains(text, true)) {
                                newList.add(it)
                                info { it.toString() }
                            }
                        }
                        filesAdapter.updateList(newList)
                    } else filesAdapter.updateList(list)
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

    private fun loadFiles() {
        doAsync {
            swipeRefreshLayout.isRefreshing = true
            list = Helper.getInstance().getFiles(java.io.File(Environment.getExternalStorageDirectory().absolutePath))
            uiThread {
                swipeRefreshLayout.isRefreshing = false
                filesAdapter.updateList(list)
            }
        }
    }

    private fun sendResultBack(file: File) {
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_FILE, file)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
