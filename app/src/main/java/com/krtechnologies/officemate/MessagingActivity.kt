package com.krtechnologies.officemate

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.krtechnologies.officemate.adapters.MessagesAdapter
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.models.Message
import com.krtechnologies.officemate.models.MessageViewModel
import kotlinx.android.synthetic.main.activity_messaging.*
import org.jetbrains.anko.*
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import java.io.IOException


class MessagingActivity : AppCompatActivity(), AnkoLogger {

    private var messagesAdapter: MessagesAdapter? = null
    private var listMessages: MutableList<Message>? = null
    private var newListMessages: MutableList<Message>? = null
    private var inputMethodManager: InputMethodManager? = null


    private var isSearchExpanded = false

    // request codes
    private val REQUEST_CODE_CONTACT = 1
    private val REQUEST_IMAGE_VIDEO_SELECT = 3
    private val REQUEST_IMAGE_CAPTURE = 4
    private val REQUEST_VIDEO_CAPTURE = 5
    private val REQUEST_FILE_SELECT = 6

    private lateinit var messageViewModel: MessageViewModel

    private var isMessageMode: Boolean = false


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



        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            stackFromEnd = true
        }

        rvMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> Helper.getInstance().hideKeyboard(recyclerView!!)
                }
            }
        })

        messagesAdapter?.let {
            rvMessages.adapter = it
        }

        ivBack.setOnClickListener {
            if (isSearchExpanded)
                hideSearchEditText()
        }

        btnSend.setOnClickListener {
            connectWithJabber()
        }


        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        messageViewModel.getAllMessages().observe(this, Observer<MutableList<Message>> {
            messagesAdapter?.updateList(it!!)
        })

        etSearch.setOnEditorActionListener { _, action, _ ->
            when (action) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    Helper.getInstance().hideKeyboard(etSearch)
                    true
                }
                else -> false
            }
        }

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty() && !isMessageMode) {
                        popUp(btnSend, btnRecord)
                        isMessageMode = true
                    } else if (it.isEmpty() && isMessageMode) {
                        popUp(btnRecord, btnSend)
                        isMessageMode = false
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        btnCamera.setOnClickListener {
            showCameraOptions()
        }

        btnContact.setOnClickListener {
            pickContact()
        }

        btnAttachment.setOnClickListener {
            dispatchSelectFileIntent()
        }

        btnGallery.setOnClickListener {
            dispatchSelectImageVideoIntent()
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
                val animView = ViewAnimationUtils.createCircularReveal(searchContainer, searchContainer.right - ((searchContainer.right / 2) / 6), searchContainer.top + (searchContainer.height / 2), startRadius.toFloat(), 0f)
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

    private fun popUp(showing: View, hiding: View) {

        val animScaleX = ObjectAnimator.ofFloat(showing, View.SCALE_X.name, 0f, 1f)
        val animScaleY = ObjectAnimator.ofFloat(showing, View.SCALE_Y.name, 0f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animScaleX, animScaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = OvershootInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                if (showing.visibility == View.GONE)
                    showing.visibility = View.VISIBLE
                if (hiding.visibility == View.VISIBLE)
                    hiding.visibility = View.GONE
            }

        })
        animatorSet.start()
    }


    private fun dispatchSelectImageVideoIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.type = "video/*"
        startActivityForResult(Intent.createChooser(intent, "Select image or video"), REQUEST_IMAGE_VIDEO_SELECT)
    }


    private fun dispatchSelectFileIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FILE_SELECT)
    }

    // this function show the options of Camera or Gallery to the user
    private fun showCameraOptions() {
        selector(null, listOf("Take Picture", "Record Video")
        ) { _, position ->
            when (position) {
                0 -> dispatchTakePictureIntent()

                1 -> dispatchTakeVideoIntent()
            }
        }
    }

    private var photoFile: java.io.File? = null

    // this function dispatches the intent to start the camera and capture image
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go

            try {
                photoFile = Helper.getInstance().createImageFile()
                //mCurrentPhotoPath = photoFile?.absolutePath
            } catch (ex: IOException) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(this,
                        "com.krtechnologies.officemate.fileprovider",
                        photoFile!!)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private var videoFile: java.io.File? = null
    private fun dispatchTakeVideoIntent() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)

        // Ensure that there's a camera activity to handle the intent
        if (takeVideoIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go

            try {
                videoFile = Helper.getInstance().createVideoFile()
                //mCurrentPhotoPath = photoFile?.absolutePath
            } catch (ex: IOException) {

            }
            // Continue only if the File was successfully created
            if (videoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(this,
                        "com.krtechnologies.officemate.fileprovider",
                        videoFile!!)
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }


    private fun pickContact() {
        val contactPickerIntent = Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(contactPickerIntent, REQUEST_CODE_CONTACT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_VIDEO_SELECT && resultCode == Activity.RESULT_OK) {
            data?.run {
                toast("Ok")
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoFile?.run {
                toast(name)
                galleryAddFile(photoFile!!)
            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
            videoFile?.run {
                toast(name)
                galleryAddFile(videoFile!!)
            }
        } else if (requestCode == REQUEST_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            data?.run {
                toast(getData().path)
            }
        } else if (requestCode == REQUEST_CODE_CONTACT && resultCode == Activity.RESULT_OK) {
            data?.run {
                contactPicked(data)
            }
        }
    }

    private fun galleryAddFile(file: java.io.File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    private fun contactPicked(data: Intent) {
        var cursor: Cursor? = null
        try {
            val phoneNo: String
            val name: String
            // getData() method will have the Content Uri of the selected contact
            val uri = data.data
            //Query the content uri
            cursor = contentResolver.query(uri, null, null, null, null)
            cursor.moveToFirst()
            // column index of the phone number
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            // column index of the contact name
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            phoneNo = cursor.getString(phoneIndex)
            name = cursor.getString(nameIndex)
            toast(name)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    inline fun SpannableStringBuilder.withSpan(span: Any, action: SpannableStringBuilder.() -> Unit): SpannableStringBuilder {
        val from = length
        action()
        setSpan(span, from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return this
    }

    private fun connectWithJabber() {
        doAsync {
            val config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword("user1", "12345")
                    .setHost("10.0.2.2")
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setServiceName("localhost")
                    .setPort(5222)
                    .setDebuggerEnabled(true) // to view what's happening in detail
                    .build()

            val conn1 = XMPPTCPConnection(config)
            try {
                conn1.connect()
                if (conn1.isConnected) {
                    info { "conn done" }
                }
                conn1.login()

                if (conn1.isAuthenticated) {
                    info { "Auth done" }
                }
            } catch (e: Exception) {
                info { e.toString() }
            }
        }
    }

}
