@file:Suppress("DEPRECATION")

package com.krtechnologies.officemate.helpers

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.krtechnologies.officemate.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.util.DisplayMetrics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import android.annotation.TargetApi
import android.content.res.Resources
import android.graphics.Point
import android.provider.ContactsContract
import android.text.format.Formatter
import android.util.Size
import android.view.WindowManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlin.collections.ArrayList


/**
 * This project is created by Kamran Ramzan on 10-Aug-18.
 */
class Helper {

    private var context: Context? = null
    private lateinit var inputMethodManager: InputMethodManager


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

        const val BASE_URL = "https://kamranramzan098.000webhostapp.com"
    }

    fun init(context: Context) {
        this.context = context
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private var gson: Gson? = null
    fun getGson(): Gson {
        if (gson == null) {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.setDateFormat("M/d/yy hh:mm a")
            gson = gsonBuilder.create()
        }
        return gson!!
    }

    // changes the color filter of drawable to accent color
    fun changeToAccent(drawable: Drawable) {
        drawable.mutate()
        drawable.setColorFilter(context!!.resources.getColor(R.color.colorAccentDark), PorterDuff.Mode.SRC_ATOP)
    }

    // changes the color filter of drawable to red color
    fun changeToSecondary(drawable: Drawable) {
        drawable.mutate()
        drawable.setColorFilter(context!!.resources.getColor(R.color.colorSecondary), PorterDuff.Mode.SRC_ATOP)
    }

    // changes the color filter of drawable to primary color
    fun changeToPrimary(drawable: Drawable) {
        drawable.mutate()
        drawable.setColorFilter(context!!.resources.getColor(R.color.colorPrimaryLight), PorterDuff.Mode.SRC_ATOP)
    }

    fun changeToBlack(drawable: Drawable) {
        drawable.mutate()
        drawable.setColorFilter(context!!.resources.getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP)
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(Environment.getExternalStorageDirectory(), File.separator + "Office Mate/")
        if (!storageDir.exists())
            storageDir.mkdirs()
        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }

    @Throws(IOException::class)
    fun createVideoFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "MP4_" + timeStamp + "_"
        val storageDir = File(Environment.getExternalStorageDirectory(), File.separator + "Office Mate/")
        if (!storageDir.exists())
            storageDir.mkdirs()

        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName, /* prefix */
                ".mp4", /* suffix */
                storageDir      /* directory */
        )
    }

    @SuppressLint("NewApi")
    fun getPath(uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))

                return getDataColumn(context!!, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context!!, contentUri!!, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context!!, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri, selection: String?,
                              selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun convertDpToPixel(dp: Float): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getTimeInMillis(): Long = System.currentTimeMillis()

    fun dpToPx(dp: Int): Int {
        return ((dp * Resources.getSystem().displayMetrics.density)).toInt()
    }

    fun getScreenHeight(c: Context): Int {
        val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    fun getScreenWidth(c: Context): Int {
        val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun getScreenRatio(c: Context): Float {
        val metrics = c.resources.displayMetrics
        return metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
    }

    fun getScreenRotation(c: Context): Int {
        val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.rotation
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun sizeToSize(sizes: Array<android.util.Size>): ArrayList<Size> {
        val size = ArrayList<Size>(sizes.size)
        for (i in sizes.indices) {
            size[i] = Size(sizes[i].width, sizes[i].height)
        }
        return size
    }

    fun hideKeyboard(view: View) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard() {
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

}