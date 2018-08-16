package com.krtechnologies.officemate

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krprojects.aira.Aira
import com.krtechnologies.officemate.helpers.Helper
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.File
import java.io.IOException


class SignUpActivity : AppCompatActivity() {

    // properties
    private var mCurrentPhotoPath: String? = null
    private var profileImage: Bitmap? = null
    private val PERMISION_CONSTANT: Int = 101
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_SELECT = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making the status bar transparent
        setStatusBarColor()

        setContentView(R.layout.activity_sign_up)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ivProfilePicture.setOnClickListener {
            checkPermissionFirst()
        }

        btnCancelProfilePicture.setOnClickListener {
            if (profileImage != null) {
                profileImage = null
                ivProfilePicture.setImageDrawable(resources.getDrawable(R.drawable.person))
                popDown()
            }
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        etName.setOnFocusChangeListener { _, b -> if (b) Helper.getInstance().changeToAccent(etName.compoundDrawables[0]) else Helper.getInstance().changeToPrimary(etName.compoundDrawables[0]) }
        etEmail.setOnFocusChangeListener { _, b -> if (b) Helper.getInstance().changeToAccent(etEmail.compoundDrawables[0]) else Helper.getInstance().changeToPrimary(etEmail.compoundDrawables[0]) }
        etOrganizationName.setOnFocusChangeListener { _, b -> if (b) Helper.getInstance().changeToAccent(etOrganizationName.compoundDrawables[0]) else Helper.getInstance().changeToPrimary(etOrganizationName.compoundDrawables[0]) }
        etDesignation.setOnFocusChangeListener { _, b -> if (b) Helper.getInstance().changeToAccent(etDesignation.compoundDrawables[0]) else Helper.getInstance().changeToPrimary(etDesignation.compoundDrawables[0]) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
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

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        if (profileImage != null)
            popUp()
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

    private var photoFile: File? = null

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


    private fun popUp() {

        val animScaleX = ObjectAnimator.ofFloat(btnCancelProfilePicture, View.SCALE_X.name, 0f, 1f)
        val animScaleY = ObjectAnimator.ofFloat(btnCancelProfilePicture, View.SCALE_Y.name, 0f, 1f)

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
                if (btnCancelProfilePicture.visibility == View.GONE)
                    btnCancelProfilePicture.visibility = View.VISIBLE
            }

        })
        animatorSet.start()

    }

    private fun popDown() {

        val animScaleX = ObjectAnimator.ofFloat(btnCancelProfilePicture, View.SCALE_X.name, 1f, 0f)
        val animScaleY = ObjectAnimator.ofFloat(btnCancelProfilePicture, View.SCALE_Y.name, 1f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animScaleX, animScaleY)
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (btnCancelProfilePicture.visibility == View.VISIBLE)
                    btnCancelProfilePicture.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        animatorSet.start()

    }
}
