package com.krtechnologies.officemate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krprojects.aira.Aira
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_profile_settings.*
import org.jetbrains.anko.doBeforeSdk
import org.jetbrains.anko.doFromSdk
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException

class ProfileSettingsActivity : AppCompatActivity() {

    // properties
    private var mCurrentPhotoPath: String? = null
    private var profileImage: Bitmap? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_SELECT = 2
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)
        initViews()

    }

    private fun initViews() {

        tvName.text = PreferencesManager.getInstance().getUserName()
        tvDesignation.text = PreferencesManager.getInstance().getUserDesignation()

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivProfilePicture.setOnClickListener {
            checkPermissionFirst()
        }

        Glide.with(this)
                .asBitmap()
                .load(PreferencesManager.getInstance().getProfilePicture())
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(150f).toInt(), Helper.getInstance().convertDpToPixel(150f).toInt()).fallback(R.drawable.person).error(R.drawable.person))
                .into(object : Target<Bitmap> {
                    override fun onLoadStarted(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                    }

                    override fun getSize(cb: SizeReadyCallback) {
                    }

                    override fun getRequest(): Request? {
                        return null
                    }

                    override fun onStop() {
                    }

                    override fun setRequest(request: Request?) {
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onStart() {
                    }

                    override fun onDestroy() {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        ivProfilePicture.setImageBitmap(resource)
                    }

                })
    }

    // this function checks for the permissions and then shows the image options
    @SuppressLint("InlinedApi")
    private fun checkPermissionFirst() {
        doFromSdk(Build.VERSION_CODES.M) {
            if (Aira.checkPermission(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA))) {
                showImageOptions()
            } else {
                Aira.requestPermission(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA),
                        10, "Need Permission", "Office Mate Needs Camera and Storage Permission to perform the intended task.",
                        object : Aira.OnPermissionResultListener {
                            override fun onPermissionGranted(p0: MutableList<String>?) {
                                if (p0?.size!! > 0)
                                    showImageOptions()
                            }

                            override fun onPermissionFailed(p0: MutableList<String>?) {
                                if (p0?.size!! > 0)
                                    toast("Failed")
                            }

                        })
            }
        }

        doBeforeSdk(Build.VERSION_CODES.M) {
            showImageOptions()
        }
    }

    // this function show the options of Camera or Gallery to the user
    private fun showImageOptions() {
        selector(null, listOf("Camera", "Gallery")
        ) { _, position ->
            when (position) {
                0 -> dispatchTakePictureIntent()
                1 -> dispatchSelectImageIntent()
            }
        }
    }

    // this function dispatches the intent to start the camera and capture image
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go

            try {
                photoFile = Helper.getInstance().createImageFile()
                mCurrentPhotoPath = photoFile?.absolutePath
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


    private fun dispatchSelectImageIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_SELECT)
    }

    // After capturing the image through camera, this function adds the captured image to gallery
    private fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Aira.onRequestPermissionResult(requestCode, permissions)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Aira.onActivityResult(requestCode)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(Uri.fromFile(photoFile))
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this)

            galleryAddPic()
        } else if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            toast(Helper.getInstance().getPath(data.data!!)!!)
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(data.data)
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this)

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            val resultUri: Uri = result.uri
            Glide.with(this)
                    .asBitmap()
                    .load(resultUri)
                    .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(150f).toInt(), Helper.getInstance().convertDpToPixel(150f).toInt()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).fallback(R.drawable.person).error(R.drawable.person))
                    .into(object : Target<Bitmap> {
                        override fun onLoadStarted(placeholder: Drawable?) {
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                        }

                        override fun getSize(cb: SizeReadyCallback) {
                        }

                        override fun getRequest(): Request? {
                            return null
                        }

                        override fun onStop() {
                        }

                        override fun setRequest(request: Request?) {
                        }

                        override fun removeCallback(cb: SizeReadyCallback) {
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                        override fun onStart() {
                        }

                        override fun onDestroy() {
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            ivProfilePicture.setImageBitmap(resource)
                            profileImage = resource
                        }

                    })
        }
    }
}
