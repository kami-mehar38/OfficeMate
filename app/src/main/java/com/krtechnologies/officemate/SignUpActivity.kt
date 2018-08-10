package com.krtechnologies.officemate

import android.app.Activity
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_sign_up.*
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.os.Build
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.krprojects.aira.Aira
import com.krtechnologies.officemate.helpers.Helper


class SignUpActivity : AppCompatActivity() {

    // properties
    private var mCurrentPhotoPath: String? = null
    private val PERMISION_CONSTANT: Int = 101
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ivProfilePicture.setOnClickListener {
            checkPermissionFirst()
        }


        etName.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
        etEmail.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
        etOrganizationName.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
        etDesignation.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Aira.onActivityResult(requestCode)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                    .load(mCurrentPhotoPath)
                    .apply(RequestOptions().override(150, 150).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
                    .into(ivProfilePicture);
            galleryAddPic()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Aira.onRequestPermissionResult(requestCode, permissions)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // this function checks for the permissions and then shows the image options
    private fun checkPermissionFirst() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (Aira.checkPermission(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA))) {
                showImageOptions()
            } else {
                Aira.requestPermission(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA),
                        PERMISION_CONSTANT, "Need Permission", "Office Mate Needs Camera and Storage Permission to perform the intended task.",
                        object : Aira.OnPermissionResultListener {
                            override fun onPermissionGranted(p0: MutableList<String>?) {
                                if (p0?.size!! > 0)
                                    toast("Granted")
                            }

                            override fun onPermissionFailed(p0: MutableList<String>?) {
                                if (p0?.size!! > 0)
                                    toast("Failed")
                            }

                        })
            }
        else {
            showImageOptions()
        }

    }

    // this function show the options of Camera or Gallery to the user
    private fun showImageOptions() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setItems(R.array.image_pick_options) { _: DialogInterface, position: Int -> if (position == 0) dispatchTakePictureIntent() else selectImage() }
        alertDialogBuilder.create().show()
    }

    // this function dispatches the intent to start the camera and capture image
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = Helper.getInstance().createImageFile()
                mCurrentPhotoPath = photoFile.absolutePath
            } catch (ex: IOException) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(this,
                        "com.krtechnologies.officemate.fileprovider",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun selectImage() {

    }

    // After capturing the image through camera, this function adds the captured image to gallery
    private fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    // simple extension function to show the toast
    private fun SignUpActivity.toast(message: String) {
        Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
    }

}
