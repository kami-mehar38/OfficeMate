@file:Suppress("DEPRECATION")

package com.krtechnologies.officemate.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.os.Environment
import android.widget.EditText
import com.krtechnologies.officemate.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * This project is created by Kamran Ramzan on 10-Aug-18.
 */
class Helper {

    private var context: Context? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var helper: Helper? = null

        @Synchronized
        fun getInstance(): Helper {
            synchronized(Helper::class) {
                if (helper == null)
                    helper = Helper()
                return helper!!
            }
        }
    }

    fun init(context: Context) {
        this.context = context
    }

    // changes the color filter of drawable to accent color
    fun changeToAccent(editText: EditText) {
        editText.compoundDrawables?.get(0)?.setColorFilter(context!!.resources.getColor(R.color.colorAccentDark), PorterDuff.Mode.SRC_ATOP)
    }

    // changes the color filter of drawable to primary color
    fun changeToPrimary(editText: EditText) {
        editText.compoundDrawables?.get(0)?.setColorFilter(context!!.resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }
}