package com.krtechnologies.officemate

import android.annotation.TargetApi
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.krtechnologies.officemate.helpers.Camera2Source
import com.krtechnologies.officemate.helpers.CameraSource
import android.view.SurfaceView
import com.krtechnologies.officemate.helpers.Helper
import java.io.IOException
import android.os.Build
import android.view.SurfaceHolder
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView


/**
 * Created by ingizly on 9/10/18
 **/
class CameraSourcePreview : ViewGroup {

    private val TAG = "CameraSourcePreview"

    //PREVIEW VISUALIZERS FOR BOTH CAMERA1 AND CAMERA2 API.
    private var mSurfaceView: SurfaceView? = null
    private var mAutoFitTextureView: AutoFitTextureView? = null

    private var usingCameraOne: Boolean = false
    private var mStartRequested: Boolean = false
    private var mSurfaceAvailable: Boolean = false
    private var viewAdded = false

    //CAMERA SOURCES FOR BOTH CAMERA1 AND CAMERA2 API.
    private var mCameraSource: CameraSource? = null
    private var mCamera2Source: Camera2Source? = null

    private var mOverlay: GraphicOverlay? = null
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenRotation: Int = 0

    init {
        screenHeight = Helper.getInstance().getScreenHeight(context)
        screenWidth = Helper.getInstance().getScreenWidth(context)
        screenRotation = Helper.getInstance().getScreenRotation(context)
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surface: SurfaceHolder) {
                mSurfaceAvailable = true
                mOverlay?.bringToFront()
                try {
                    startIfReady()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not start camera source.", e)
                }

            }

            override fun surfaceDestroyed(surface: SurfaceHolder) {
                mSurfaceAvailable = false
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        })
        mAutoFitTextureView = AutoFitTextureView(context)
        mAutoFitTextureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                mSurfaceAvailable = true
                mOverlay?.bringToFront()
                try {
                    startIfReady()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not start camera source.", e)
                }

            }

            override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                mSurfaceAvailable = false
                return true
            }

            override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource, overlay: GraphicOverlay) {
        usingCameraOne = true
        mOverlay = overlay
        start(cameraSource)
    }

    @Throws(IOException::class)
    fun start(camera2Source: Camera2Source, overlay: GraphicOverlay) {
        usingCameraOne = false
        mOverlay = overlay
        start(camera2Source)
    }

    @Throws(IOException::class)
    private fun start(cameraSource: CameraSource?) {
        if (cameraSource == null) {
            stop()
        }
        mCameraSource = cameraSource
        if (mCameraSource != null) {
            mStartRequested = true
            if (!viewAdded) {
                addView(mSurfaceView)
                viewAdded = true
            }
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }

        }
    }

    @Throws(IOException::class)
    private fun start(camera2Source: Camera2Source?) {
        if (camera2Source == null) {
            stop()
        }
        mCamera2Source = camera2Source
        if (mCamera2Source != null) {
            mStartRequested = true
            if (!viewAdded) {
                addView(mAutoFitTextureView)
                viewAdded = true
            }
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }

        }
    }

    fun stop() {
        mStartRequested = false
        if (usingCameraOne) {
            if (mCameraSource != null) {
                mCameraSource?.stop()
            }
        } else {
            mCamera2Source?.stop()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(IOException::class)
    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            try {
                if (usingCameraOne) {
                    mCameraSource?.start(mSurfaceView?.holder!!)
                    if (mOverlay != null) {
                        val size = mCameraSource?.getPreviewSize()
                        if (size != null) {
                            val min = Math.min(size.width, size.height)
                            val max = Math.max(size.width, size.height)
                            // FOR GRAPHIC OVERLAY, THE PREVIEW SIZE WAS REDUCED TO QUARTER
                            // IN ORDER TO PREVENT CPU OVERLOAD
                            mOverlay?.setCameraInfo(min / 4, max / 4, mCameraSource?.getCameraFacing()!!)
                            mOverlay?.clear()
                        } else {
                            stop()
                        }
                    }
                    mStartRequested = false
                } else {
                    mCamera2Source?.start(mAutoFitTextureView!!, screenRotation)
                    if (mOverlay != null) {
                        val size = mCamera2Source?.getPreviewSize()
                        if (size != null) {
                            val min = Math.min(size.width, size.height)
                            val max = Math.max(size.width, size.height)
                            // FOR GRAPHIC OVERLAY, THE PREVIEW SIZE WAS REDUCED TO QUARTER
                            // IN ORDER TO PREVENT CPU OVERLOAD
                            mOverlay?.setCameraInfo(min / 4, max / 4, mCamera2Source?.getCameraFacing()!!)
                            mOverlay?.clear()
                        } else {
                            stop()
                        }
                    }
                    mStartRequested = false
                }
            } catch (e: SecurityException) {
                Log.d(TAG, "SECURITY EXCEPTION: $e")
            }

        }
    }

    private val mSurfaceViewListener = object : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            mOverlay?.bringToFront()
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }

        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            mSurfaceAvailable = true
            mOverlay?.bringToFront()
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }

        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            mSurfaceAvailable = false
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var width = 480
        var height = 720
        if (usingCameraOne) {
            if (mCameraSource != null) {
                val size = mCameraSource?.getPreviewSize()
                if (size != null) {
                    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
                    height = size.width
                    width = size.height
                }
            }
        } else {
            if (mCamera2Source != null) {
                val size = mCamera2Source?.getPreviewSize()
                if (size != null) {
                    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
                    height = size.width
                    width = size.height
                }
            }
        }

        //RESIZE PREVIEW IGNORING ASPECT RATIO. THIS IS ESSENTIAL.
        val newWidth = (height * screenWidth) / screenHeight

        val layoutWidth: Float = (right - left).toFloat()
        val layoutHeight: Float = (bottom - top).toFloat()
        // Computes height and width for potentially doing fit width.
        var childWidth = layoutWidth
        var childHeight = ((layoutWidth / newWidth) * height)
        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight
            childWidth = ((layoutHeight / height) * newWidth)
        }
        for (i in 0 until childCount) {
            getChildAt(i).layout(0, 0, childWidth.toInt(), childHeight.toInt()); }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

}