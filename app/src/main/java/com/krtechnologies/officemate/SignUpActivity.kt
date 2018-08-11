package com.krtechnologies.officemate

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
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
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krprojects.aira.Aira
import com.krtechnologies.officemate.helpers.Helper
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class SignUpActivity : AppCompatActivity() {

    // properties
    private var mCurrentPhotoPath: String? = null
    private var profileImage: Bitmap? = null
    private val PERMISION_CONSTANT: Int = 101
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_SELECT = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ivProfilePicture.setOnClickListener {
            checkPermissionFirst()
        }

        btnCancelProfilePicture.setOnClickListener {
            if (profileImage != null) {
                profileImage = null
                ivProfilePicture.setImageDrawable(resources.getDrawable(R.drawable.person))
            }
        }

        etName.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
        etEmail.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
        etOrganizationName.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
        etDesignation.setOnFocusChangeListener { view, b -> if (b) Helper.getInstance().changeToAccent(view as EditText) else Helper.getInstance().changeToPrimary(view as EditText) }
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
                    .apply(RequestOptions().override(250, 250).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).fallback(R.drawable.person).error(R.drawable.person))
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
                                    showImageOptions()
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
        alertDialogBuilder.setItems(R.array.image_pick_options) { _: DialogInterface, position: Int -> if (position == 0) dispatchTakePictureIntent() else dispatchSelectImageIntent() }
        alertDialogBuilder.create().show()
    }

    var photoFile: File? = null

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

    // simple extension function to show the toast
    private fun SignUpActivity.toast(message: String) {
        Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        popUp()
    }

    private fun popUp() {

        val scaleX = ObjectAnimator.ofInt(btnCancelProfilePicture, "scaleX", 0, 1)
        val scaleY = ObjectAnimator.ofInt(btnCancelProfilePicture, "scaleY", 0, 1)

        val animator = AnimatorSet()
        animator.playTogether(scaleX, scaleY)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                btnCancelProfilePicture.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.interpolator = OvershootInterpolator()
        animator.duration = 500
        animator.start()
    }
}
