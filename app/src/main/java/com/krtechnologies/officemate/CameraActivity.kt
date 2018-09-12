package com.krtechnologies.officemate

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.krtechnologies.officemate.helpers.Camera2Source
import com.krtechnologies.officemate.helpers.CameraSource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)

class CameraActivity : AppCompatActivity(), AnkoLogger {

    private val MEDIA_TYPE_IMAGE = 1
    private val MEDIA_TYPE_VIDEO = 2

    // CAMERA VERSION ONE DECLARATIONS
    private var mCameraSource: CameraSource? = null

    // CAMERA VERSION TWO DECLARATIONS
    private var mCamera2Source: Camera2Source? = null

    // COMMON TO BOTH CAMERAS
    private var mPreview: CameraSourcePreview? = null
    private var previewFaceDetector: FaceDetector? = null
    private var mGraphicOverlay: GraphicOverlay? = null
    private var mFaceGraphic: FaceGraphic? = null
    private var wasActivityResumed = false
    private var isRecordingVideo = false
    private var takePictureButton: Button? = null
    private var switchButton: Button? = null
    private var videoButton: Button? = null
    private var cameraVersion: TextView? = null
    private var ivAutoFocus: ImageView? = null

    // DEFAULT CAMERA BEING OPENED
    private val usingFrontCamera = true

    // MUST BE CAREFUL USING THIS VARIABLE.
    // ANY ATTEMPT TO START CAMERA2 ON API < 21 WILL CRASH.
    private var useCamera2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_camera)

        takePictureButton = findViewById(R.id.btn_takepicture)
        switchButton = findViewById(R.id.btn_switch)
        videoButton = findViewById(R.id.btn_video)
        mPreview = findViewById(R.id.preview)
        mGraphicOverlay = findViewById(R.id.faceOverlay)
        cameraVersion = findViewById(R.id.cameraVersion)
        ivAutoFocus = findViewById(R.id.ivAutoFocus)

        useCamera2 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        createCameraSourceFront()
    }

    override fun onResume() {
        super.onResume()
        if (wasActivityResumed)
        //If the CAMERA2 is paused then resumed, it won't start again unless creating the whole camera again.
            if (useCamera2) {
                if (usingFrontCamera) {
                    createCameraSourceFront()
                } else {
                    createCameraSourceBack()
                }
            } else {
                startCameraSource()
            }
    }

    override fun onPause() {
        super.onPause()
        wasActivityResumed = true
        stopCameraSource()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCameraSource()
        if (previewFaceDetector != null) {
            previewFaceDetector?.release()
        }
    }

    private fun createCameraSourceFront() {
        previewFaceDetector = FaceDetector.Builder(this@CameraActivity)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build()

        if (previewFaceDetector?.isOperational!!) {
            previewFaceDetector?.setProcessor(MultiProcessor.Builder(GraphicFaceTrackerFactory()).build())
        } else {
            Toast.makeText(this@CameraActivity, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show()
        }

        if (useCamera2) {
            mCamera2Source = Camera2Source.Builder(this@CameraActivity, previewFaceDetector!!)
                    .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                    .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                    .setFacing(Camera2Source.CAMERA_FACING_FRONT)
                    .build()

            //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
            //WE WILL USE CAMERA1.
            if (mCamera2Source?.isCamera2Native()!!) {
                startCameraSource()
            } else {
                useCamera2 = false
                if (usingFrontCamera) createCameraSourceFront(); else createCameraSourceBack()
            }
        } else {
            mCameraSource = CameraSource.Builder(this@CameraActivity, previewFaceDetector!!)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(30.0f)
                    .build()

            startCameraSource()
        }
    }

    private fun createCameraSourceBack() {
        previewFaceDetector =  FaceDetector.Builder(this@CameraActivity)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        if(previewFaceDetector?.isOperational()!!) {
            previewFaceDetector?.setProcessor( MultiProcessor.Builder( GraphicFaceTrackerFactory()).build());
        } else {
            Toast.makeText(this@CameraActivity, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        if(useCamera2) {
            mCamera2Source =  Camera2Source.Builder(this@CameraActivity, previewFaceDetector!!)
                    .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                    .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                    .setFacing(Camera2Source.CAMERA_FACING_BACK)
                    .build();

            //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
            //WE WILL USE CAMERA1.
            if(mCamera2Source?.isCamera2Native()!!) {
                startCameraSource();
            } else {
                useCamera2 = false;
                if(usingFrontCamera) createCameraSourceFront(); else createCameraSourceBack();
            }
        } else {
            mCameraSource =  CameraSource.Builder(this@CameraActivity, previewFaceDetector!!)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(30.0f)
                    .build();

            startCameraSource();
        }
    }

    private inner class GraphicFaceTrackerFactory : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return GraphicFaceTracker(mGraphicOverlay!!)
        }
    }

    private inner class GraphicFaceTracker internal constructor(private val mOverlay: GraphicOverlay) : Tracker<Face>() {

        init {
            mFaceGraphic = FaceGraphic(mOverlay, this@CameraActivity)
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face?) {
            mFaceGraphic?.setId(faceId)
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        override fun onUpdate(detectionResults: Detector.Detections<Face>?, face: Face?) {
            mOverlay.add(mFaceGraphic!!)
            mFaceGraphic?.updateFace(face!!)
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        override fun onMissing(detectionResults: Detector.Detections<Face>?) {
            mFaceGraphic?.goneFace()
            mOverlay.remove(mFaceGraphic!!)
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        override fun onDone() {
            mFaceGraphic?.goneFace()
            mOverlay.remove(mFaceGraphic!!)
        }
    }

    private fun startCameraSource() {
        if (useCamera2) {
            if (mCamera2Source != null) {
                cameraVersion?.setText("Camera 2")
                try {
                    mPreview?.start(mCamera2Source!!, mGraphicOverlay!!)
                } catch (e: IOException) {
                    Log.e("", "Unable to start camera source 2.", e)
                    mCamera2Source?.release()
                    mCamera2Source = null
                }

            }
        } else {
            if (mCameraSource != null) {
                cameraVersion?.setText("Camera 1")
                try {
                    mPreview?.start(mCameraSource!!, mGraphicOverlay!!)
                } catch (e: IOException) {
                    Log.e("", "Unable to start camera source.", e)
                    mCameraSource?.release()
                    mCameraSource = null
                }

            }
        }
    }

    private fun stopCameraSource() {
        mPreview?.stop()
    }


    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                resources.getString(R.string.app_name)
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    info { "failed to create directory" }
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }

    /** Check if this device has a camera */
    private fun checkCameraHardware(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }


}
