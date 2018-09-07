package com.krtechnologies.officemate.helpers

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.IOException
import android.view.View


/**
 * This project is created by Kamran Ramzan on 07-Sep-18.
 */
/** A basic Camera preview class */
class CameraPreview(
        context: Context,
        private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback, AnkoLogger {

    private lateinit var mPreviewSize: Camera.Size
    private lateinit var mSupportedPreviewSizes: MutableList<Camera.Size>

    private val mHolder: SurfaceHolder = holder.apply {
        mSupportedPreviewSizes = mCamera.parameters.supportedPreviewSizes
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        addCallback(this@CameraPreview)
        // deprecated setting, but required on Android versions prior to 3.0
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera.apply {
            try {
                mSupportedPreviewSizes = parameters.supportedPreviewSizes
                val parameters = parameters
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height)
                this.parameters = parameters
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                info { "Error setting camera preview: ${e.message}" }
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        mCamera.apply {
            try {
                mSupportedPreviewSizes = parameters.supportedPreviewSizes
                val parameters = parameters
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height)
                this.parameters = parameters
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                info { "Error setting camera preview: ${e.message}" }
            }
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = View.resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)

        mSupportedPreviewSizes.let {
            mPreviewSize = getOptimalPreviewSize(it, width, height)!!
        }
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w

        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = java.lang.Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }
}