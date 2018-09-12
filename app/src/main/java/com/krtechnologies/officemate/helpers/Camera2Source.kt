package com.krtechnologies.officemate.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.*
import android.support.annotation.Nullable
import android.support.annotation.RequiresPermission
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.Surface
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.krtechnologies.officemate.AutoFitTextureView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * Created by ingizly on 9/9/18
 **/
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Source : AnkoLogger {

    companion object {
        var CAMERA_FACING_BACK = 0
        var CAMERA_FACING_FRONT = 1

        val CAMERA_FLASH_OFF = CaptureRequest.CONTROL_AE_MODE_OFF
        val CAMERA_FLASH_ON = CaptureRequest.CONTROL_AE_MODE_ON
        val CAMERA_FLASH_AUTO = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
        val CAMERA_FLASH_ALWAYS = CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
        val CAMERA_FLASH_REDEYE = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE

        val CAMERA_AF_AUTO = CaptureRequest.CONTROL_AF_MODE_AUTO
        val CAMERA_AF_EDOF = CaptureRequest.CONTROL_AF_MODE_EDOF
        val CAMERA_AF_MACRO = CaptureRequest.CONTROL_AF_MODE_MACRO
        val CAMERA_AF_OFF = CaptureRequest.CONTROL_AF_MODE_OFF
        val CAMERA_AF_CONTINUOUS_PICTURE = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        val CAMERA_AF_CONTINUOUS_VIDEO = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO

        private val TAG = "Camera2Source"
        private val ratioTolerance = 0.1
        private val maxRatioTolerance = 0.18

        private val ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }
        private val INVERSE_ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 270)
            append(Surface.ROTATION_90, 180)
            append(Surface.ROTATION_180, 90)
            append(Surface.ROTATION_270, 0)
        }

        /**
         * Max preview width that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_WIDTH = 1920

        /**
         * Max preview height that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_HEIGHT = 1080


        /**
         * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as the
         * respective max size, and whose aspect ratio matches with the specified value. If such size
         * doesn't exist, choose the largest one that is at most as large as the respective max size,
         * and whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended output
         *                          class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal {@code Size}, or an arbitrary one if none were big enough
         */
        private fun chooseOptimalSize(choices: ArrayList<Size>, textureViewWidth: Int, textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {

            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            // Collect the supported resolutions that are smaller than the preview Surface
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                        option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth &&
                            option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            if (bigEnough.size > 0) {
                return Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                return Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size")
                return choices[0]
            }
        }

        /**
         * We choose a video size with 3x4 aspect ratio. Also, we don't use sizes
         * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
         *
         * @param choices The list of available sizes
         * @return The video size
         */
        fun chooseVideoSize(choices: ArrayList<Size>): Size {
            for (size in choices) {
                if (size.width == size.height * 16 / 9) {
                    return size
                }
            }
            Log.e(TAG, "Couldn't find any suitable video size")
            return choices[0]
        }

    }


    private var mContext: Context? = null

    private var mFacing = CAMERA_FACING_BACK
    private var mFlashMode = CAMERA_FLASH_AUTO
    private var mFocusMode = CAMERA_AF_AUTO
    private var cameraStarted = false
    private var mSensorOrientation: Int = 0

    /**
     * A reference to the opened [CameraDevice].
     */
    private var mCameraDevice: CameraDevice? = null

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var mBackgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var mBackgroundHandler: Handler? = null

    /**
     * Camera state: Showing camera preview.
     */
    private val STATE_PREVIEW = 0

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private val STATE_WAITING_LOCK = 1

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private val STATE_WAITING_PRECAPTURE = 2

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private val STATE_WAITING_NON_PRECAPTURE = 3

    /**
     * Camera state: Picture was taken.
     */
    private val STATE_PICTURE_TAKEN = 4

    private var mDisplayOrientation: Int = 0

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    /**
     * [CaptureRequest] generated by [.mPreviewRequestBuilder]
     */
    private var mPreviewRequest: CaptureRequest? = null

    /**
     * The current state of camera state for taking pictures.
     *
     * @see .mCaptureCallback
     */
    private var mState = STATE_PREVIEW

    /**
     * A [CameraCaptureSession] for camera preview.
     */
    private var mCaptureSession: CameraCaptureSession? = null

    /**
     * The [Size] of camera preview.
     */
    private var mPreviewSize: Size? = null

    /**
     * The [Size] of Media Recorder.
     */
    private var mVideoSize: Size? = null

    private var mMediaRecorder: MediaRecorder? = null
    private val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private var videoFile: String? = null
    private var videoStartCallback: VideoStartCallback? = null
    private var videoStopCallback: VideoStopCallback? = null
    private var videoErrorCallback: VideoErrorCallback? = null

    /**
     * ID of the current [CameraDevice].
     */
    private var mCameraId: String? = null

    /**
     * An [AutoFitTextureView] for camera preview.
     */
    private var mTextureView: AutoFitTextureView? = null

    private var mShutterCallback: ShutterCallback? = null

    private var mAutoFocusCallback: AutoFocusCallback? = null

    private var sensorArraySize: Rect? = null
    private var isMeteringAreaAFSupported = false
    private var swappedDimensions = false

    private var manager: CameraManager? = null

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val mCameraOpenCloseLock = Semaphore(1)

    /**
     * Whether the current camera device supports Flash or not.
     */
    private var mFlashSupported: Boolean = false

    /**
     * Dedicated thread and associated runnable for calling into the detector with frames, as the
     * frames become available from the camera.
     */
    private var mProcessingThread: Thread? = null
    private var mFrameProcessor: FrameProcessingRunnable? = null

    /**
     * An [ImageReader] that handles still image capture.
     */
    private var mImageReaderStill: ImageReader? = null

    /**
     * An [ImageReader] that handles live preview.
     */
    private var mImageReaderPreview: ImageReader? = null

    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                }// We have nothing to do when the camera preview is working normally.
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState
                            || CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState
                            || CaptureRequest.CONTROL_AF_STATE_INACTIVE == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            if (request.tag === "FOCUS_TAG") {
                //The focus trigger is complete!
                //Resume repeating request, clear AF trigger.
                mAutoFocusCallback?.onAutoFocus(true)
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, null)
                mPreviewRequestBuilder?.setTag("")
                mPreviewRequest = mPreviewRequestBuilder?.build()
                try {
                    mCaptureSession?.setRepeatingRequest(mPreviewRequest, this, mBackgroundHandler)
                } catch (ex: CameraAccessException) {
                    info { "AUTO FOCUS FAILURE: $ex" }
                }

            } else {
                process(result)
            }
        }

        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
            if (request.tag === "FOCUS_TAG") {
                info { "Manual AF failure: $failure" }
                mAutoFocusCallback?.onAutoFocus(false)
            }
        }

    }

    /**
     * This is a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * preview frame is ready to be processed.
     */
    private val mOnPreviewAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val mImage = reader.acquireNextImage() ?: return@OnImageAvailableListener
        mFrameProcessor?.setNextFrame(convertYUV420888ToNV21(mImage))
        mImage.close()
    }

    /**
     * This is a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val mOnImageAvailableListener = PictureDoneCallback()

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
     */
    private val mStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }
    }

    //==============================================================================================
    // Builder
    //==============================================================================================

    /**
     * Builder for configuring and creating an associated camera source.
     */

    class Builder(private val context: Context, val detector: Detector<*>) {

        private var mDetector: Detector<*>? = null
        private var mCameraSource = Camera2Source()

        init {
            mDetector = detector
            mCameraSource.mContext = context
        }

        fun setFocusMode(mode: Int): Builder {
            mCameraSource.mFocusMode = mode
            return this
        }

        fun setFlashMode(mode: Int): Builder {
            mCameraSource.mFlashMode = mode
            return this
        }

        /**
         * Sets the camera to use (either {@link #CAMERA_FACING_BACK} or
         * {@link #CAMERA_FACING_FRONT}). Default: back facing.
         */
        fun setFacing(facing: Int): Builder {
            if ((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT)) {
                throw  IllegalArgumentException("Invalid camera: $facing")
            }
            mCameraSource.mFacing = facing
            return this
        }

        /**
         * Creates an instance of the camera source.
         */
        fun build(): Camera2Source {
            mCameraSource.mFrameProcessor = mCameraSource.FrameProcessingRunnable(mDetector!!)
            return mCameraSource
        }
    }

    //==============================================================================================
    // Bridge Functionality for the Camera2 API
    //==============================================================================================

    /**
     * Callback interface used to signal the moment of actual image capture.
     */
    interface ShutterCallback {
        /**
         * Called as near as possible to the moment when a photo is captured from the sensor. This
         * is a good opportunity to play a shutter sound or give other feedback of camera operation.
         * This may be some time after the photo was triggered, but some time before the actual data
         * is available.
         */
        fun onShutter()
    }

    /**
     * Callback interface used to supply image data from a photo capture.
     */
    interface PictureCallback {
        /**
         * Called when image data is available after a picture is taken.  The format of the data
         * is a JPEG Image.
         */
        fun onPictureTaken(image: Image)
    }

    /**
     * Callback interface used to indicate when video Recording Started.
     */
    interface VideoStartCallback {
        fun onVideoStart()
    }

    interface VideoStopCallback {
        //Called when Video Recording stopped.
        fun onVideoStop(videoFile: String)
    }

    interface VideoErrorCallback {
        //Called when error ocurred while recording video.
        fun onVideoError(error: String)
    }

    /**
     * Callback interface used to notify on completion of camera auto focus.
     */
    interface AutoFocusCallback {
        /**
         * Called when the camera auto focus completes.  If the camera
         * does not support auto-focus and autoFocus is called,
         * onAutoFocus will be called immediately with a fake value of
         * `success` set to `true`.
         *
         *
         * The auto-focus routine does not lock auto-exposure and auto-white
         * balance after it completes.
         *
         * @param success true if focus was successful, false if otherwise
         */
        fun onAutoFocus(success: Boolean)
    }

    //==============================================================================================
    // Public
    //==============================================================================================

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        try {
            if (mBackgroundThread != null) {
                mBackgroundThread?.quitSafely()
                mBackgroundThread?.join()
                mBackgroundThread = null
                mBackgroundHandler = null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Stops the camera and releases the resources of the camera and underlying detector.
     */
    fun release() {
        mFrameProcessor?.release()
        stop()
    }

    /**
     * Closes the camera and stops sending frames to the underlying frame detector.
     * <p/>
     * This camera source may be restarted again by calling {@link #start(AutoFitTextureView, int)}.
     * <p/>
     * Call {@link #release()} instead to completely shut down this camera source and release the
     * resources of the underlying detector.
     */
    fun stop() {
        try {
            mFrameProcessor?.setActive(false)
            if (mProcessingThread != null) {
                try {
                    // Wait for the thread to complete to ensure that we can't have multiple threads
                    // executing at the same time (i.e., which would happen if we called start too
                    // quickly after stop).
                    mProcessingThread?.join()
                } catch (e: InterruptedException) {
                    info { "Frame processing thread interrupted on release." }
                }
                mProcessingThread = null
            }
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession?.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice?.close()
                mCameraDevice = null
            }
            if (null != mImageReaderPreview) {
                mImageReaderPreview?.close()
                mImageReaderPreview = null
            }
            if (null != mImageReaderStill) {
                mImageReaderStill?.close()
                mImageReaderStill = null
            }
        } catch (e: InterruptedException) {
            throw  RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
            stopBackgroundThread()
        }
    }

    fun isCamera2Native(): Boolean {
        try {
            if (ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            manager = mContext?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            mCameraId = manager?.cameraIdList?.get(mFacing)
            val characteristics = manager?.getCameraCharacteristics(mCameraId)
            //CHECK CAMERA HARDWARE LEVEL. IF CAMERA2 IS NOT NATIVELY SUPPORTED, GO BACK TO CAMERA1
            val deviceLevel = characteristics?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            return deviceLevel != null && (deviceLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY)
        } catch (ex: CameraAccessException) {
            return false
        } catch (e: NullPointerException) {
            return false
        } catch (ez: ArrayIndexOutOfBoundsException) {
            return false
        }
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector.  The supplied
     * texture view is used for the preview so frames can be displayed to the user.
     *
     * @param textureView the surface holder to use for the preview frames
     * @param displayOrientation the display orientation for a non stretched preview
     * @throws IOException if the supplied texture view could not be used as the preview display
     */

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class)
    fun start(textureView: AutoFitTextureView, displayOrientation: Int): Camera2Source {
        mDisplayOrientation = displayOrientation
        if (ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraStarted) {
                return this
            }
            cameraStarted = true
            startBackgroundThread()

            mProcessingThread = Thread(mFrameProcessor)
            mFrameProcessor?.setActive(true)
            mProcessingThread?.start()

            mTextureView = textureView
            if (mTextureView?.isAvailable!!) {
                setUpCameraOutputs(mTextureView?.width!!, mTextureView?.height!!)
            }
        }
        return this
    }

    /**
     * Returns the preview size that is currently in use by the underlying camera.
     */
    fun getPreviewSize(): Size? {
        return mPreviewSize
    }

    /**
     * Returns the selected camera; one of [.CAMERA_FACING_BACK] or
     * [.CAMERA_FACING_FRONT].
     */
    fun getCameraFacing(): Int {
        return mFacing
    }

    fun autoFocus(@Nullable cb: AutoFocusCallback, pEvent: MotionEvent, screenW: Int, screenH: Int) {
        mAutoFocusCallback = cb
        if (sensorArraySize != null) {
            val y: Int = (pEvent.x / screenW * sensorArraySize!!.height()).toInt()
            val x: Int = (pEvent.y / screenH * sensorArraySize!!.width()).toInt()
            val halfTouchWidth = 150
            val halfTouchHeight = 150
            val focusAreaTouch = MeteringRectangle(
                    Math.max(x - halfTouchWidth, 0),
                    Math.max(y - halfTouchHeight, 0),
                    halfTouchWidth * 2,
                    halfTouchHeight * 2,
                    MeteringRectangle.METERING_WEIGHT_MAX - 1)

            try {
                mCaptureSession?.stopRepeating()
                //Cancel any existing AF trigger (repeated touches, etc.)
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                mCaptureSession?.capture(mPreviewRequestBuilder?.build(), mCaptureCallback, mBackgroundHandler)

                //Now add a new AF trigger with focus region
                if (isMeteringAreaAFSupported) {
                    mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(focusAreaTouch))
                }
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
                mPreviewRequestBuilder?.setTag("FOCUS_TAG") //we'll capture this later for resuming the preview!
                //Then we ask for a single request (not repeating!)
                mCaptureSession?.capture(mPreviewRequestBuilder?.build(), mCaptureCallback, mBackgroundHandler)
            } catch (ex: CameraAccessException) {
                info { "AUTO FOCUS EXCEPTION: $ex" }
            }
        }
    }

    /**
     * Initiate a still image capture. The camera preview is suspended
     * while the picture is being taken, but will resume once picture taking is done.
     */
    fun takePicture(shutter: ShutterCallback, picCallback: PictureCallback) {
        mShutterCallback = shutter
        mOnImageAvailableListener.mDelegate = picCallback
        lockFocus()
    }

    fun recordVideo(videoStartCallback: VideoStartCallback, videoStopCallback: VideoStopCallback, videoErrorCallback: VideoErrorCallback) {
        try {
            this.videoStartCallback = videoStartCallback
            this.videoStopCallback = videoStopCallback
            this.videoErrorCallback = videoErrorCallback
            if (mCameraDevice == null || !mTextureView?.isAvailable!! || mPreviewSize == null) {
                this.videoErrorCallback?.onVideoError("Camera not ready.")
                return
            }
            videoFile = "${Environment.getExternalStorageDirectory()}/${formatter.format(Date())}.mp4"
            mMediaRecorder = MediaRecorder()
            //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder?.setOutputFile(videoFile)
            mMediaRecorder?.setVideoEncodingBitRate(10000000)
            mMediaRecorder?.setVideoFrameRate(30)
            mMediaRecorder?.setVideoSize(mVideoSize?.width!!, mVideoSize!!.height)
            mMediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            if (swappedDimensions) {
                mMediaRecorder?.setOrientationHint(INVERSE_ORIENTATIONS.get(mDisplayOrientation))
            } else {
                mMediaRecorder?.setOrientationHint(ORIENTATIONS.get(mDisplayOrientation))
            }
            mMediaRecorder?.prepare()
            closePreviewSession()
            createCameraRecordSession()
        } catch (ex: IOException) {
            info { ex.message }
        }
    }

    fun stopVideo() {
        //Stop recording
        mMediaRecorder?.stop()
        mMediaRecorder?.reset()
        videoStopCallback?.onVideoStop(videoFile!!)
        closePreviewSession()
        createCameraPreviewSession()
    }

    fun getBestAspectPictureSize(supportedPictureSizes: Array<Size>): Size {
        val targetRatio = Helper.getInstance().getScreenRatio(mContext!!)
        var bestSize: Size? = null
        val diffs = TreeMap<Double, List<android.util.Size>>()

        //Select supported sizes which ratio is less than ratioTolerance

        for (size in supportedPictureSizes) {
            val ratio = (size.width / size.height).toFloat()
            val diff = Math.abs(ratio - targetRatio).toDouble()
            if (diff < ratioTolerance) {
                if (diffs.keys.contains(diff)) {
                    //add the value to the list
                    diffs[diff]?.plus(size)
                } else {
                    val newList = ArrayList<Size>()
                    newList.add(size)
                    diffs[diff] = newList
                }
            }
        }

        //If no sizes were supported, (strange situation) establish a higher ratioTolerance
        if (diffs.isEmpty()) {
            for (size in supportedPictureSizes) {
                val ratio = (size.width / size.height).toFloat()
                val diff = Math.abs(ratio - targetRatio).toDouble()
                if (diff < maxRatioTolerance) {
                    if (diffs.keys.contains(diff)) {
                        //add the value to the list
                        diffs[diff]?.plus(size)
                    } else {
                        val newList = ArrayList<Size>()
                        newList.add(size)
                        diffs[diff] = newList
                    }
                }
            }
        }

        //Select the highest resolution from the ratio filtered ones.
        for (entry in diffs.entries) {
            val entries = entry.value
            for (index in 0 until entries.size) {
                val s = entries.get(index)
                if (bestSize == null) {
                    bestSize = Size(s.width, s.height)
                } else if (bestSize.width < s.width || bestSize.height < s.height) {
                    bestSize = Size(s.width, s.height)
                }
            }
        }
        return bestSize!!
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    fun configureTransform(viewWidth: Float, viewHeight: Float) {
        if (null == mTextureView || null == mPreviewSize) {
            return
        }
        val rotation = mDisplayOrientation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth, viewHeight)
        val bufferRect = RectF(0f, 0f, mPreviewSize!!.height.toFloat(), mPreviewSize!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    viewHeight / mPreviewSize!!.height,
                    viewWidth / mPreviewSize!!.width)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        mTextureView?.setTransform(matrix)
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    fun setUpCameraOutputs(width: Int, height: Int) {
        try {
            if (ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw  RuntimeException("Time out waiting to lock camera opening.")
            }
            if (manager == null) manager = mContext?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            mCameraId = manager?.cameraIdList?.get(mFacing)
            val characteristics = manager?.getCameraCharacteristics(mCameraId)
            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: return

            // For still image captures, we use the largest available size.
            val largest = getBestAspectPictureSize(map.getOutputSizes(ImageFormat.JPEG))
            mImageReaderStill = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, /*maxImages*/2)
            mImageReaderStill?.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler)

            sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val maxAFRegions = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)
            if (maxAFRegions != null) {
                isMeteringAreaAFSupported = maxAFRegions >= 1
            }
            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            if (sensorOrientation != null) {
                mSensorOrientation = sensorOrientation

                when (mDisplayOrientation) {
                    Surface.ROTATION_0,
                    Surface.ROTATION_180 -> {
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true
                        }
                    }

                    Surface.ROTATION_90,
                    Surface.ROTATION_270 -> {
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true
                        }
                    }
                    else ->
                        Log.e(TAG, "Display rotation is invalid: $mDisplayOrientation")
                }
            }

            val displaySize = Point(Helper.getInstance().getScreenWidth(mContext!!), Helper.getInstance().getScreenHeight(mContext!!))
            var rotatedPreviewWidth = width
            var rotatedPreviewHeight = height
            var maxPreviewWidth = displaySize.x
            var maxPreviewHeight = displaySize.y

            if (swappedDimensions) {
                rotatedPreviewWidth = height
                rotatedPreviewHeight = width
                maxPreviewWidth = displaySize.y
                maxPreviewHeight = displaySize.x
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT
            }

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            val outputSizes: ArrayList<Size> = Helper.getInstance().sizeToSize(map.getOutputSizes(SurfaceTexture::class.java))
            val outputSizesMediaRecorder = Helper.getInstance().sizeToSize(map.getOutputSizes(MediaRecorder::class.java))
            mPreviewSize = chooseOptimalSize(outputSizes, rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest)
            mVideoSize = chooseVideoSize(outputSizesMediaRecorder)

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            val orientation = mDisplayOrientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView?.setAspectRatio(mPreviewSize?.width!!, mPreviewSize?.height!!)
            } else {
                mTextureView?.setAspectRatio(mPreviewSize?.height!!, mPreviewSize?.width!!)
            }

            // Check if the flash is supported.
            val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            mFlashSupported = available ?: available

            configureTransform(width.toFloat(), height.toFloat())

            manager?.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw  RuntimeException("Interrupted while trying to lock camera opening.", e)
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            info { e.message }
        }
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK
            mPreviewRequest = mPreviewRequestBuilder?.build()
            mCaptureSession?.capture(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            if (mFlashSupported) {
                mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode)
            }
            mCaptureSession?.capture(mPreviewRequestBuilder?.build(), mCaptureCallback, mBackgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW
            mCaptureSession?.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE
            mCaptureSession?.capture(mPreviewRequestBuilder?.build(), mCaptureCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private fun captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return
            }
            if (mShutterCallback != null) {
                mShutterCallback?.onShutter()
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder?.addTarget(mImageReaderStill?.surface)

            // Use the same AE and AF modes as the preview.
            captureBuilder?.set(CaptureRequest.CONTROL_AF_MODE, mFocusMode)
            if (mFlashSupported) {
                captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode)
            }

            // Orientation
            captureBuilder?.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(mDisplayOrientation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
                    unlockFocus()
                }
            }

            mCaptureSession?.stopRepeating()
            mCaptureSession?.capture(captureBuilder?.build(), CaptureCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession?.close()
            mCaptureSession = null
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            val texture = mTextureView?.surfaceTexture


            // We configure the size of default buffer to be the size of camera preview we want.
            texture?.setDefaultBufferSize(mPreviewSize?.width!!, mPreviewSize?.height!!)

            mImageReaderPreview = ImageReader.newInstance(mPreviewSize?.width!!, mPreviewSize?.height!!, ImageFormat.YUV_420_888, 1)
            mImageReaderPreview?.setOnImageAvailableListener(mOnPreviewAvailableListener, mBackgroundHandler)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder?.addTarget(surface)
            mPreviewRequestBuilder?.addTarget(mImageReaderPreview?.surface)

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice?.createCaptureSession(Arrays.asList(surface, mImageReaderPreview?.surface, mImageReaderStill?.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    if (null == mCameraDevice) {
                        return
                    }

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = session

                    try {
                        // Auto focus should be continuous for camera preview.
                        mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, mFocusMode)
                        if (mFlashSupported) {
                            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode)
                        }

                        // Finally, we start displaying the camera preview.
                        mPreviewRequest = mPreviewRequestBuilder?.build()!!
                        mCaptureSession?.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraRecordSession() {
        try {
            val texture = mTextureView?.surfaceTexture


            // We configure the size of default buffer to be the size of camera preview we want.
            texture?.setDefaultBufferSize(mPreviewSize?.width!!, mPreviewSize?.height!!)

            mImageReaderPreview = ImageReader.newInstance(mPreviewSize?.width!!, mPreviewSize?.height!!, ImageFormat.YUV_420_888, 1)
            mImageReaderPreview?.setOnImageAvailableListener(mOnPreviewAvailableListener, mBackgroundHandler)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // Set up Surface for the MediaRecorder
            val recorderSurface = mMediaRecorder?.surface

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            mPreviewRequestBuilder?.addTarget(surface)
            mPreviewRequestBuilder?.addTarget(mImageReaderPreview?.surface)
            mPreviewRequestBuilder?.addTarget(recorderSurface)

            // Start a capture session
            mCameraDevice?.createCaptureSession(Arrays.asList(surface, mImageReaderPreview?.surface, recorderSurface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    // The camera is already closed
                    if (mCameraDevice == null) {
                        return
                    }

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = session

                    try {
                        // Auto focus should be continuous for camera preview.
                        mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, mFocusMode)
                        if (mFlashSupported) {
                            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode)
                        }

                        // Finally, we start displaying the camera preview.
                        mPreviewRequest = mPreviewRequestBuilder?.build()
                        mCaptureSession?.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }

                    //Start recording
                    mMediaRecorder?.start()
                    videoStartCallback?.onVideoStart()
                }
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    inner class FrameProcessingRunnable(private val detector: Detector<*>) : Runnable {
        private var mDetector: Detector<*>? = null
        private val mStartTimeMillis = SystemClock.elapsedRealtime()

        // This lock guards all of the member variables below.
        private val mLock = Object()
        private var mActive = true

        // These pending variables hold the state associated with the new frame awaiting processing.
        private var mPendingTimeMillis: Long? = null
        private var mPendingFrameId = 0
        private var mPendingFrameData: ByteArray? = null

        init {
            mDetector = detector
        }

        /**
         * Releases the underlying receiver.  This is only safe to do after the associated thread
         * has completed, which is managed in camera source's release method above.
         */
        @SuppressLint("Assert")
        fun release() {
            assert(mProcessingThread?.state == Thread.State.TERMINATED)
            mDetector?.release()
            mDetector = null
        }

        /**
         * Marks the runnable as active/not active.  Signals any blocked threads to continue.
         */
        fun setActive(active: Boolean) {
            synchronized(mLock) {
                mActive = active
                mLock.notifyAll()
            }
        }

        /**
         * Sets the frame data received from the camera.
         */
        fun setNextFrame(data: ByteArray) {
            synchronized(mLock) {
                if (mPendingFrameData != null) {
                    mPendingFrameData = null
                }

                // Timestamp and frame ID are maintained here, which will give downstream code some
                // idea of the timing of frames received and when frames were dropped along the way.
                mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTimeMillis
                mPendingFrameId++
                mPendingFrameData = data

                // Notify the processor thread if it is waiting on the next frame (see below).
                mLock.notifyAll()
            }
        }

        /**
         * As long as the processing thread is active, this executes detection on frames
         * continuously.  The next pending frame is either immediately available or hasn't been
         * received yet.  Once it is available, we transfer the frame info to local variables and
         * run detection on that frame.  It immediately loops back for the next frame without
         * pausing.
         * <p/>
         * If detection takes longer than the time in between new frames from the camera, this will
         * mean that this loop will run without ever waiting on a frame, avoiding any context
         * switching or frame acquisition time latency.
         * <p/>
         * If you find that this is using more CPU than you'd like, you should probably decrease the
         * FPS setting above to allow for some idle time in between frames.
         */
        override fun run() {
            var outputFrame: Frame? = null

            while (true) {
                synchronized(mLock) {
                    while (mActive && (mPendingFrameData == null)) {
                        try {
                            // Wait for the next frame to be received from the camera, since we
                            // don't have it yet.
                            mLock.wait()
                        } catch (e: InterruptedException) {
                            Log.d(TAG, "Frame processing loop terminated.", e)
                            return
                        }
                    }

                    if (!mActive) {
                        // Exit the loop once this camera source is stopped or released.  We check
                        // this here, immediately after the wait() above, to handle the case where
                        // setActive(false) had been called, triggering the termination of this
                        // loop.
                        return
                    }

                    outputFrame = Frame.Builder()
                            .setImageData(ByteBuffer.wrap(quarterNV21(mPendingFrameData!!, mPreviewSize?.width!!, mPreviewSize?.height!!)), mPreviewSize?.width!! / 4, mPreviewSize?.height!! / 4, ImageFormat.NV21)
                            .setId(mPendingFrameId)
                            .setTimestampMillis(mPendingTimeMillis!!)
                            .setRotation(getDetectorOrientation(mSensorOrientation))
                            .build()

                    // We need to clear mPendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    mPendingFrameData = null
                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    mDetector?.receiveFrame(outputFrame)
                } catch (t: Throwable) {
                    Log.e(TAG, "Exception thrown from receiver.", t)
                }
            }
        }

    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private fun getOrientation(rotation: Int): Int {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
    }

    private fun getDetectorOrientation(sensorOrientation: Int): Int {
        return when (sensorOrientation) {
            0 -> Frame.ROTATION_0
            90 -> Frame.ROTATION_90
            180 -> Frame.ROTATION_180
            270 -> Frame.ROTATION_270
            360 -> Frame.ROTATION_0
            else -> Frame.ROTATION_90
        }
    }

    private fun convertYUV420888ToNV21(imgYUV420: Image): ByteArray {
        // Converting YUV_420_888 data to NV21.
        val data: ByteArray
        val buffer0 = imgYUV420.planes[0].buffer
        val buffer2 = imgYUV420.planes[2].buffer
        val buffer0_size = buffer0.remaining()
        val buffer2_size = buffer2.remaining()
        data = ByteArray(buffer0_size + buffer2_size)
        buffer0.get(data, 0, buffer0_size)
        buffer2.get(data, buffer0_size, buffer2_size)
        return data
    }

    private fun quarterNV21(data: ByteArray, iWidth: Int, iHeight: Int): ByteArray {
        // Reduce to quarter size the NV21 frame
        val yuv = ByteArray(iWidth / 4 * iHeight / 4 * 3 / 2)
        // halve yuma
        var i = 0
        var y = 0
        while (y < iHeight) {
            var x = 0
            while (x < iWidth) {
                yuv[i] = data[y * iWidth + x]
                i++
                x += 4
            }
            y += 4
        }
        // halve U and V color components
        /*
        for (int y = 0; y < iHeight / 2; y+=4) {
            for (int x = 0; x < iWidth; x += 8) {
                yuv[i] = data[(iWidth * iHeight) + (y * iWidth) + x];
                i++;
                yuv[i] = data[(iWidth * iHeight) + (y * iWidth) + (x + 1)];
                i++;
            }
        }*/
        return yuv
    }

    inner class PictureDoneCallback : ImageReader.OnImageAvailableListener {

        var mDelegate: PictureCallback? = null

        override fun onImageAvailable(reader: ImageReader?) {
            if (mDelegate != null) {
                mDelegate?.onPictureTaken(reader?.acquireNextImage()!!)
            }
        }

    }
}