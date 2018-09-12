package com.krtechnologies.officemate.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.support.annotation.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by ingizly on 9/10/18
 **/
class CameraSource {

    companion object {

        val CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK
        val CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT

        private val TAG = "CameraSource"
        private val ratioTolerance = 0.1
        private val maxRatioTolerance = 0.15

        @JvmStatic
        fun getBestAspectPictureSize(camera: Camera, context: Context): Camera.Size {
            val parameters = camera.parameters
            val supportedPictureSizes = parameters.supportedPictureSizes
            val targetRatio: Float = Helper.getInstance().getScreenRatio(context)
            var bestSize: Camera.Size? = null
            val diffs = TreeMap<Double, List<Camera.Size>>()

            for (size in supportedPictureSizes) {
                val ratio: Float = (size.width / size.height).toFloat()
                val diff: Double = (Math.abs(ratio - targetRatio)).toDouble()
                if (diff < ratioTolerance) {
                    if (diffs.keys.contains(diff)) {
                        //add the value to the list
                        diffs[diff]?.plus(size)
                    } else {
                        val newList = ArrayList<Camera.Size>()
                        newList.add(size)
                        diffs[diff] = newList
                    }
                }
            }

            if (diffs.isEmpty()) {
                for (size in supportedPictureSizes) {
                    val ratio: Float = (size.width / size.height).toFloat()
                    val diff: Double = (Math.abs(ratio - targetRatio)).toDouble()
                    if (diff < maxRatioTolerance) {
                        if (diffs.keys.contains(diff)) {
                            //add the value to the list
                            diffs[diff]?.plus(size)
                        } else {
                            val newList = ArrayList<Camera.Size>()
                            newList.add(size)
                            diffs[diff] = newList
                        }
                    }
                }
            }

            //diffs now contains all of the usable sizes
            //now let's see which one has the least amount of
            for (entry in diffs.entries) {
                val entries = entry.value

                for (i in 0 until entries.size) {
                    val s = entries[i]
                    if (bestSize == null) {
                        bestSize = s
                    } else if (bestSize.width < s.width || bestSize.height < s.height) {
                        bestSize = s
                    }
                }
            }
            if (bestSize == null) return supportedPictureSizes.get(0)
            return bestSize
        }

        @JvmStatic
        fun getBestAspectPreviewSize(camera: Camera, context: Context): Camera.Size {
            val parameters = camera.parameters
            val supportedPreviewSizes = parameters.supportedPreviewSizes
            val targetRatio: Float = Helper.getInstance().getScreenRatio(context)
            var bestSize: Camera.Size? = null
            val diffs = TreeMap<Double, List<Camera.Size>>()

            for (size in supportedPreviewSizes) {
                val ratio: Float = (size.width / size.height).toFloat()
                val diff: Double = (Math.abs(ratio - targetRatio)).toDouble()
                if (diff < ratioTolerance) {
                    if (diffs.keys.contains(diff)) {
                        //add the value to the list
                        diffs.get(diff)?.plus(size)
                    } else {
                        val newList = ArrayList<Camera.Size>()
                        newList.add(size)
                        diffs.put(diff, newList)
                    }
                }
            }

            if (diffs.isEmpty()) {
                for (size in supportedPreviewSizes) {
                    val ratio: Float = (size.width / size.height).toFloat()
                    val diff: Double = (Math.abs(ratio - targetRatio)).toDouble()
                    if (diff < maxRatioTolerance) {
                        if (diffs.keys.contains(diff)) {
                            //add the value to the list
                            diffs.get(diff)?.plus(size)
                        } else {
                            val newList = ArrayList<Camera.Size>()
                            newList.add(size)
                            diffs.put(diff, newList)
                        }
                    }
                }
            }

            //diffs now contains all of the usable sizes
            //now let's see which one has the least amount of
            for (entry in diffs.entries) {
                val entries = entry.value
                for (i in 0 until entries.size) {
                    val s = entries[i]
                    if (s.height <= 1080 && s.width <= 1920) {
                        if (bestSize == null) {
                            bestSize = s
                        } else if (bestSize.width < s.width || bestSize.height < s.height) {
                            bestSize = s
                        }
                    }
                }
            }
            if (bestSize == null) return supportedPreviewSizes.get(0)
            return bestSize
        }

        //RESIZE PREVIEW FRAMES TO HALF FOR A FASTER FACE DETECTION
        private fun quarterNV21(d: ByteBuffer, imageWidth: Int, imageHeight: Int): ByteBuffer {
            val data = d.array()
            val yuv = ByteArray(imageWidth / 4 * imageHeight / 4 * 3 / 2)
            // halve yuma
            var i = 0
            run {
                var y = 0
                while (y < imageHeight) {
                    var x = 0
                    while (x < imageWidth) {
                        yuv[i] = data[y * imageWidth + x]
                        i++
                        x += 4
                    }
                    y += 4
                }
            }
            // halve U and V color components
            var y = 0
            while (y < imageHeight / 2) {
                var x = 0
                while (x < imageWidth) {
                    yuv[i] = data[imageWidth * imageHeight + y * imageWidth + x]
                    i++
                    yuv[i] = data[imageWidth * imageHeight + y * imageWidth + (x + 1)]
                    i++
                    x += 8
                }
                y += 4
            }
            //REDUCED TO QUARTER QUALITY AND ONLY IN GRAY SCALE!
            return ByteBuffer.wrap(yuv)
        }
    }

    @StringDef(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_EDOF, Camera.Parameters.FOCUS_MODE_FIXED, Camera.Parameters.FOCUS_MODE_INFINITY, Camera.Parameters.FOCUS_MODE_MACRO)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class FocusMode

    @StringDef(Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_RED_EYE, Camera.Parameters.FLASH_MODE_TORCH)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class FlashMode

    @IntDef(CamcorderProfile.QUALITY_LOW, CamcorderProfile.QUALITY_HIGH, CamcorderProfile.QUALITY_480P, CamcorderProfile.QUALITY_720P, CamcorderProfile.QUALITY_1080P, CamcorderProfile.QUALITY_2160P, CamcorderProfile.QUALITY_CIF, CamcorderProfile.QUALITY_QCIF, CamcorderProfile.QUALITY_QVGA)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class VideoMode


    private var mContext: Context? = null

    private val mCameraLock = Any()

    // Guarded by mCameraLock
    private var mCamera: Camera? = null

    private var mFacing = CAMERA_FACING_BACK

    /**
     * Rotation of the device, and thus the associated preview images captured from the device.
     * See [Frame.Metadata.getRotation].
     */
    private var mRotation: Int = 0

    private var mPreviewSize: Size? = null
    private var mPictureSize: Size? = null

    // These values may be requested by the caller.  Due to hardware limitations, we may need to
    // select close, but not exactly the same values for these.
    private var mRequestedFps = 30.0f

    private val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private var mediaRecorder: MediaRecorder? = null
    private var videoFile: String? = null
    private var videoStartCallback: VideoStartCallback? = null
    private var videoStopCallback: VideoStopCallback? = null
    private var videoErrorCallback: VideoErrorCallback? = null

    private var mFocusMode = Camera.Parameters.FOCUS_MODE_AUTO
    private var mFlashMode = Camera.Parameters.FLASH_MODE_AUTO

    private var previewSurfaceHolder: SurfaceHolder? = null

    /**
     * Dedicated thread and associated runnable for calling into the detector with frames, as the
     * frames become available from the camera.
     */
    private var mProcessingThread: Thread? = null
    private var mFrameProcessor: FrameProcessingRunnable? = null

    /**
     * Map to convert between a byte array, received from the camera, and its associated byte
     * buffer.  We use byte buffers internally because this is a more efficient way to call into
     * native code later (avoids a potential copy).
     */
    private val mBytesToByteBuffer = HashMap<ByteArray, ByteBuffer>()

    //==============================================================================================
    // Builder
    //==============================================================================================

    /**
     * Builder for configuring and creating an associated camera source.
     */

    class Builder(private val context: Context, private val detector: Detector<*>) {
        private var usesFaceDetector = false
        private var mDetector: Detector<*>? = null
        private var mCameraSource = CameraSource()

        init {
            usesFaceDetector = true
            mDetector = detector
            mCameraSource.mContext = context
        }

        /**
         * Sets the requested frame rate in frames per second.  If the exact requested value is not
         * not available, the best matching available value is selected.   Default: 30.
         */
        fun setRequestedFps(fps: Float): Builder {
            if (fps <= 0) {
                throw IllegalArgumentException("Invalid fps: $fps")
            }
            mCameraSource.mRequestedFps = fps
            return this
        }

        fun setFocusMode(@FocusMode mode: String): Builder {
            mCameraSource.mFocusMode = mode
            return this
        }

        fun setFlashMode(@FlashMode mode: String): Builder {
            mCameraSource.mFlashMode = mode
            return this
        }

        /**
         * Sets the camera to use (either [.CAMERA_FACING_BACK] or
         * [.CAMERA_FACING_FRONT]). Default: back facing.
         */
        fun setFacing(facing: Int): Builder {
            if (facing != CAMERA_FACING_BACK && facing != CAMERA_FACING_FRONT) {
                throw IllegalArgumentException("Invalid camera: $facing")
            }
            mCameraSource.mFacing = facing
            return this
        }

        /**
         * Creates an instance of the camera source.
         */
        fun build(): CameraSource {
            if (usesFaceDetector)
                mCameraSource.mFrameProcessor = mCameraSource.FrameProcessingRunnable(mDetector!!)
            return mCameraSource
        }
    }

    //==============================================================================================
    // Bridge Functionality for the Camera1 API
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
         * is a jpeg binary.
         */
        fun onPictureTaken(pic: Bitmap)
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

    /**
     * Callback interface used to notify on auto focus start and stop.
     *
     *
     *
     * This is only supported in continuous autofocus modes -- [ ][Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO] and [ ][Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE]. Applications can show
     * autofocus animation based on this.
     */
    interface AutoFocusMoveCallback {
        /**
         * Called when the camera auto focus starts or stops.
         *
         * @param start true if focus starts to move, false if focus stops to move
         */
        fun onAutoFocusMoving(start: Boolean)
    }

    //==============================================================================================
    // Public
    //==============================================================================================

    /**
     * Stops the camera and releases the resources of the camera and underlying detector.
     */
    fun release() {
        synchronized(mCameraLock) {
            stop()
            mFrameProcessor?.release()
        }
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector.  The supplied
     * surface holder is used for the preview so frames can be displayed to the user.
     *
     * @param surfaceHolder the surface holder to use for the preview frames
     * @throws IOException if the supplied surface holder could not be used as the preview display
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class)
    fun start(surfaceHolder: SurfaceHolder): CameraSource? {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                return this
            }
            try {
                previewSurfaceHolder = surfaceHolder
                val requestedCameraId = getIdForRequestedCamera(mFacing)
                if (requestedCameraId == -1) {
                    throw RuntimeException("Could not find requested camera.")
                }

                val camera = Camera.open(requestedCameraId)

                val pictureSize = getBestAspectPictureSize(camera, mContext!!)
                val previewSize = getBestAspectPreviewSize(camera, mContext!!)
                mPreviewSize = Size(previewSize.width, previewSize.height)
                mPictureSize = Size(pictureSize.width, pictureSize.height)
                val previewFpsRange = selectPreviewFpsRange(camera, mRequestedFps)

                val parameters = camera.parameters
                parameters.setPictureSize(pictureSize.width, pictureSize.height)
                parameters.setPreviewSize(mPreviewSize?.width!!, mPreviewSize?.height!!)
                parameters.setPreviewFpsRange(previewFpsRange?.get(Camera.Parameters.PREVIEW_FPS_MIN_INDEX)!!, previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
                parameters.previewFormat = ImageFormat.NV21

                setRotation(camera, parameters, requestedCameraId)

                if (mFocusMode != null) {
                    if (parameters.supportedFocusModes.contains(mFocusMode)) {
                        parameters.focusMode = mFocusMode
                    } else {
                        Log.i(TAG, "Camera focus mode: $mFocusMode is not supported on this device.")
                    }
                }

                // setting mFocusMode to the one set in the params
                mFocusMode = parameters.focusMode

                if (mFlashMode != null) {
                    if (parameters.supportedFlashModes != null) {
                        if (parameters.supportedFlashModes.contains(mFlashMode)) {
                            parameters.flashMode = mFlashMode
                        } else {
                            Log.i(TAG, "Camera flash mode: $mFlashMode is not supported on this device.")
                        }
                    }
                }

                // setting mFlashMode to the one set in the params
                mFlashMode = parameters.flashMode

                camera.parameters = parameters

                // Four frame buffers are needed for working with the camera:
                //
                //   one for the frame that is currently being executed upon in doing detection
                //   one for the next pending frame to process immediately upon completing detection
                //   two for the frames that the camera uses to populate future preview images
                camera.setPreviewCallbackWithBuffer(CameraPreviewCallback())
                camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize!!))
                camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize!!))
                camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize!!))
                camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize!!))

                mCamera = camera

                mCamera?.setPreviewDisplay(previewSurfaceHolder)
                mCamera?.startPreview()

                mProcessingThread = Thread(mFrameProcessor)
                mFrameProcessor?.setActive(true)
                mProcessingThread?.start()

            } catch (e: RuntimeException) {
                e.printStackTrace()
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                return null
            }

        }
        return this
    }

    /**
     * Closes the camera and stops sending frames to the underlying frame detector.
     * <p/>
     * This camera source may be restarted again by calling {@link #start(SurfaceHolder)}.
     * <p/>
     * Call {@link #release()} instead to completely shut down this camera source and release the
     * resources of the underlying detector.
     */

    fun stop() {
        synchronized(mCameraLock) {
            mFrameProcessor?.setActive(false)
            if (mProcessingThread != null) {
                try {
                    // Wait for the thread to complete to ensure that we can't have multiple threads
                    // executing at the same time (i.e., which would happen if we called start too
                    // quickly after stop).
                    mProcessingThread?.join()
                } catch (e: InterruptedException) {
                    Log.d(TAG, "Frame processing thread interrupted on release.")
                }
                mProcessingThread = null
            }

            // clear the buffer to prevent oom exceptions
            mBytesToByteBuffer.clear()

            if (mCamera != null) {
                mCamera?.stopPreview()
                mCamera?.setPreviewCallbackWithBuffer(null)
                try {
                    // We want to be compatible back to Gingerbread, but SurfaceTexture
                    // wasn't introduced until Honeycomb.  Since the interface cannot use a SurfaceTexture, if the
                    // developer wants to display a preview we must use a SurfaceHolder.  If the developer doesn't
                    // want to display a preview we use a SurfaceTexture if we are running at least Honeycomb.

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mCamera?.setPreviewTexture(null)

                    } else {
                        mCamera?.setPreviewDisplay(null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to clear camera preview: $e")
                }
                mCamera?.release()
                mCamera = null
            }
        }
    }

    /*
    * Returns whether or not video can be recorded in specified quality
    */
    fun canRecordVideo(@VideoMode videoMode: Int): Boolean {
        return try {
            CamcorderProfile.get(getIdForRequestedCamera(mFacing), videoMode)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the preview size that is currently in use by the underlying camera.
     */
    fun getPreviewSize(): Size? {
        return mPreviewSize
    }

    /*
    * Returns the picture size that is currently in use
    */
    fun getPictureSize(): Size? {
        return mPictureSize
    }

    /**
     * Returns the selected camera; one of [.CAMERA_FACING_BACK] or
     * [.CAMERA_FACING_FRONT].
     */
    fun getCameraFacing(): Int {
        return mFacing
    }

    fun doZoom(scale: Float): Int {
        synchronized(mCameraLock) {
            if (mCamera == null) {
                return 0
            }
            var currentZoom = 0
            val maxZoom: Int
            val parameters = mCamera?.parameters
            if (!parameters?.isZoomSupported!!) {
                Log.w(TAG, "Zoom is not supported on this device")
                return currentZoom
            }
            maxZoom = parameters.maxZoom

            currentZoom = parameters.zoom + 1
            val newZoom: Float
            if (scale > 1) {
                newZoom = currentZoom + scale * (maxZoom / 10)
            } else {
                newZoom = currentZoom * scale
            }
            currentZoom = Math.round(newZoom) - 1
            if (currentZoom < 0) {
                currentZoom = 0
            } else if (currentZoom > maxZoom) {
                currentZoom = maxZoom
            }
            parameters.zoom = currentZoom
            mCamera?.parameters = parameters
            return currentZoom
        }
    }

    /**
     * Initiates taking a picture, which happens asynchronously.  The camera source should have been
     * activated previously with {@link #start(SurfaceHolder)}.  The camera preview is suspended
     * while the picture is being taken, but will resume once picture taking is done.
     *
     * @param shutter the callback for image capture moment, or null
     * @param jpeg    the callback for JPEG image data, or null
     */
    fun takePicture(shutter: ShutterCallback, jpeg: PictureCallback) {
        Log.d("ASD", "TRYING TO TAKE PICTURE")
        synchronized(mCameraLock) {
            if (mCamera != null) {
                setFlashMode(mFlashMode)
                val startCallback = PictureStartCallback()
                startCallback.mDelegate = shutter
                val doneCallback = PictureDoneCallback()
                doneCallback.mDelegate = jpeg
                mCamera?.takePicture(startCallback, null, null, doneCallback)
            }
        }
    }

    /**
     * Initiates recording video.
     *
     * @param videoStartCallback the callback for video recording start
     * @param videoStopCallback the callback for video recording stop
     * @param videoErrorCallback the callback for video recording error
     */
    fun recordVideo(@NonNull videoStartCallback: VideoStartCallback, @NonNull videoStopCallback: VideoStopCallback, @NonNull videoErrorCallback: VideoErrorCallback) {
        this.videoStartCallback = videoStartCallback
        this.videoStopCallback = videoStopCallback
        this.videoErrorCallback = videoErrorCallback
        if (!checkCamera()) {
            this.videoErrorCallback?.onVideoError("Camera Error")
            return
        }
        //PREPARE MEDIA RECORDER
        val cameraId = getIdForRequestedCamera(mFacing)
        //Step 0. Disable Shutter Sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val camInfo = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, camInfo)
            if (camInfo.canDisableShutterSound) {
                mCamera?.enableShutterSound(false)
            }
        }
        //Step 1. Unlock Camera
        mCamera?.unlock()
        mediaRecorder = MediaRecorder()
        //Step 2. Create Camera Profile
        val profile: CamcorderProfile
        try {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P)
        } catch (e: Exception) {
            //CAMERA QUALITY TOO LOW!!!!!!!
            releaseMediaRecorder()
            this.videoErrorCallback?.onVideoError("Camera quality too LOW")
            return
        }

        //Step 3. Set values in Profile except AUDIO settings
        mediaRecorder?.setCamera(mCamera)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder?.setOutputFormat(profile.fileFormat)
        mediaRecorder?.setVideoEncoder(profile.videoCodec)
        mediaRecorder?.setVideoEncodingBitRate(profile.videoBitRate)
        mediaRecorder?.setVideoFrameRate(profile.videoFrameRate)
        mediaRecorder?.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight)

        //Step 4. Set output file
        videoFile = "${Environment.getExternalStorageDirectory()}/${formatter.format(Date())}.mp4"
        mediaRecorder?.setOutputFile(videoFile)
        //Step 5. Set Duration
        mediaRecorder?.setMaxDuration(-1)
        try {
            mediaRecorder?.prepare()
        } catch (e: IllegalStateException) {
            releaseMediaRecorder()
            this.videoErrorCallback?.onVideoError(e.message!!)
            return
        } catch (e: IOException) {
            releaseMediaRecorder()
            this.videoErrorCallback?.onVideoError(e.message!!)
            return
        }
        mediaRecorder?.start()
        //SEND RECORDING SIGNAL!
        this.videoStartCallback?.onVideoStart()
    }

    fun stopVideo() {
        releaseMediaRecorder()
        this.videoStopCallback?.onVideoStop(videoFile!!)
    }

    /**
     * Gets the current focus mode setting.
     *
     * @return current focus mode. This value is null if the camera is not yet created. Applications should call [ ][.autoFocus] to start the focus if focus
     * mode is FOCUS_MODE_AUTO or FOCUS_MODE_MACRO.
     * @see Camera.Parameters.FOCUS_MODE_AUTO
     *
     * @see Camera.Parameters.FOCUS_MODE_INFINITY
     *
     * @see Camera.Parameters.FOCUS_MODE_MACRO
     *
     * @see Camera.Parameters.FOCUS_MODE_FIXED
     *
     * @see Camera.Parameters.FOCUS_MODE_EDOF
     *
     * @see Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
     *
     * @see Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
     */
    @Nullable
    @FocusMode
    fun getFocusMode(): String {
        return mFocusMode
    }

    /**
     * Sets the focus mode.
     *
     * @param mode the focus mode
     * @return `true` if the focus mode is set, `false` otherwise
     * @see .getFocusMode
     */
    fun setFocusMode(@FocusMode mode: String?): Boolean {
        synchronized(mCameraLock) {
            if (mCamera != null && mode != null) {
                val parameters = mCamera?.parameters
                if (parameters?.supportedFocusModes?.contains(mode)!!) {
                    parameters.focusMode = mode
                    mCamera?.parameters = parameters
                    mFocusMode = mode
                    return true
                }
            }

            return false
        }
    }

    /**
     * Gets the current flash mode setting.
     *
     * @return current flash mode. null if flash mode setting is not
     * supported or the camera is not yet created.
     * @see Camera.Parameters.FLASH_MODE_OFF
     *
     * @see Camera.Parameters.FLASH_MODE_AUTO
     *
     * @see Camera.Parameters.FLASH_MODE_ON
     *
     * @see Camera.Parameters.FLASH_MODE_RED_EYE
     *
     * @see Camera.Parameters.FLASH_MODE_TORCH
     */
    @Nullable
    @FlashMode
    fun getFlashMode(): String {
        return mFlashMode
    }

    /**
     * Sets the flash mode.
     *
     * @param mode flash mode.
     * @return `true` if the flash mode is set, `false` otherwise
     * @see .getFlashMode
     */
    fun setFlashMode(@FlashMode mode: String?): Boolean {
        synchronized(mCameraLock) {
            if (mCamera != null && mode != null) {
                val parameters = mCamera?.parameters
                val supportedFlashModes = parameters?.supportedFlashModes
                if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size == 1 && supportedFlashModes[0] == Camera.Parameters.FLASH_MODE_OFF) {
                    return false
                }
                if (supportedFlashModes.contains(mode)) {
                    parameters.flashMode = mode
                    mCamera?.parameters = parameters
                    mFlashMode = mode
                    return true
                }
            }
            return false
        }
    }

    /**
     * Starts camera auto-focus and registers a callback function to run when
     * the camera is focused.  This method is only valid when preview is active
     * (between [.start] and before [.stop] or [.release]).
     *
     *
     *
     * Callers should check
     * [.getFocusMode] to determine if
     * this method should be called. If the camera does not support auto-focus,
     * it is a no-op and [AutoFocusCallback.onAutoFocus]
     * callback will be called immediately.
     *
     *
     *
     * If the current flash mode is not
     * [Camera.Parameters.FLASH_MODE_OFF], flash may be
     * fired during auto-focus, depending on the driver and camera hardware.
     *
     *
     *
     * @param cb the callback to run
     * @see .cancelAutoFocus
     */
    fun autoFocus(@Nullable cb: AutoFocusCallback?) {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                var autoFocusCallback: CameraAutoFocusCallback? = null
                if (cb != null) {
                    autoFocusCallback = CameraAutoFocusCallback()
                    autoFocusCallback.mDelegate = cb
                }
                mCamera?.autoFocus(autoFocusCallback)
            }
        }
    }

    /**
     * Cancels any auto-focus function in progress.
     * Whether or not auto-focus is currently in progress,
     * this function will return the focus position to the default.
     * If the camera does not support auto-focus, this is a no-op.
     *
     * @see .autoFocus
     */
    fun cancelAutoFocus() {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                mCamera?.cancelAutoFocus()
            }
        }
    }

    /**
     * Sets camera auto-focus move callback.
     *
     * @param cb the callback to run
     * @return `true` if the operation is supported (i.e. from Jelly Bean), `false` otherwise
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun setAutoFocusMoveCallback(@Nullable cb: AutoFocusMoveCallback?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false
        }

        synchronized(mCameraLock) {
            if (mCamera != null) {
                var autoFocusMoveCallback: CameraAutoFocusMoveCallback? = null
                if (cb != null) {
                    autoFocusMoveCallback = CameraAutoFocusMoveCallback()
                    autoFocusMoveCallback.mDelegate = cb
                }
                mCamera?.setAutoFocusMoveCallback(autoFocusMoveCallback)
            }
        }

        return true
    }

    //==============================================================================================
    // Private
    //==============================================================================================

    /**
     * Only allow creation via the builder class.
     */
    private fun CameraSource() {}

    /**
     * Wraps the camera1 shutter callback so that the deprecated API isn't exposed.
     */
    private inner class PictureStartCallback : Camera.ShutterCallback {
        var mDelegate: ShutterCallback? = null

        override fun onShutter() {
            mDelegate?.onShutter()
        }
    }

    /**
     * Wraps the final callback in the camera1 sequence, so that we can automatically turn the camera
     * preview back on after the picture has been taken.
     */
    private inner class PictureDoneCallback : Camera.PictureCallback {
        var mDelegate: PictureCallback? = null


        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            val pic = BitmapFactory.decodeByteArray(data, 0, data.size)
            mDelegate?.onPictureTaken(pic)

            synchronized(mCameraLock) {
                if (mCamera != null) {
                    mCamera?.startPreview()
                }
            }
        }
    }

    /**
     * Wraps the camera1 auto focus callback so that the deprecated API isn't exposed.
     */
    private inner class CameraAutoFocusCallback : Camera.AutoFocusCallback {
        var mDelegate: AutoFocusCallback? = null

        override fun onAutoFocus(success: Boolean, camera: Camera) {
            mDelegate?.onAutoFocus(success)
        }
    }

    /**
     * Wraps the camera1 auto focus move callback so that the deprecated API isn't exposed.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private inner class CameraAutoFocusMoveCallback : Camera.AutoFocusMoveCallback {
        var mDelegate: AutoFocusMoveCallback? = null

        override fun onAutoFocusMoving(start: Boolean, camera: Camera) {
            mDelegate?.onAutoFocusMoving(start)
        }
    }

    /**
     * Gets the id for the camera specified by the direction it is facing.  Returns -1 if no such
     * camera was found.
     *
     * @param facing the desired camera (front-facing or rear-facing)
     */
    private fun getIdForRequestedCamera(facing: Int): Int {
        val cameraInfo = CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == facing) {
                return i
            }
        }
        return -1
    }

    /**
     * Selects the most suitable preview frames per second range, given the desired frames per
     * second.
     *
     * @param camera            the camera to select a frames per second range from
     * @param desiredPreviewFps the desired frames per second for the camera preview frames
     * @return the selected preview frames per second range
     */
    private fun selectPreviewFpsRange(camera: Camera, desiredPreviewFps: Float): IntArray? {
        // The camera API uses integers scaled by a factor of 1000 instead of floating-point frame
        // rates.
        val desiredPreviewFpsScaled = (desiredPreviewFps * 1000.0f).toInt()

        // The method for selecting the best range is to minimize the sum of the differences between
        // the desired value and the upper and lower bounds of the range.  This may select a range
        // that the desired value is outside of, but this is often preferred.  For example, if the
        // desired frame rate is 29.97, the range (30, 30) is probably more desirable than the
        // range (15, 30).
        var selectedFpsRange: IntArray? = null
        var minDiff = Integer.MAX_VALUE
        val previewFpsRangeList = camera.parameters.supportedPreviewFpsRange
        for (range in previewFpsRangeList) {
            val deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
            val deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
            val diff = Math.abs(deltaMin) + Math.abs(deltaMax)
            if (diff < minDiff) {
                selectedFpsRange = range
                minDiff = diff
            }
        }
        return selectedFpsRange
    }

    /**
     * Calculates the correct rotation for the given camera id and sets the rotation in the
     * parameters.  It also sets the camera's display orientation and rotation.
     *
     * @param parameters the camera parameters for which to set the rotation
     * @param cameraId   the camera id to set rotation based on
     */
    private fun setRotation(camera: Camera, parameters: Camera.Parameters, cameraId: Int) {
        val windowManager = mContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var degrees = 0
        when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else ->
                Log.e(TAG, "Bad rotation value")
        }

        val cameraInfo = CameraInfo()
        Camera.getCameraInfo(cameraId, cameraInfo)

        val angle: Int
        val displayAngle: Int
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360
            displayAngle = (360 - angle) % 360 // compensate for it being mirrored
        } else {  // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360
            displayAngle = angle
        }

        // This corresponds to the rotation constants in {@link Frame}.
        mRotation = angle / 90

        camera.setDisplayOrientation(displayAngle)
        parameters.setRotation(angle)
    }

    /**
     * Creates one buffer for the camera preview callback.  The size of the buffer is based off of
     * the camera preview size and the format of the camera image.
     *
     * @return a new preview buffer of the appropriate size for the current camera settings
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createPreviewBuffer(previewSize: Size): ByteArray {
        val bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21)
        val sizeInBits = (previewSize.height * previewSize.width * bitsPerPixel).toLong()
        val bufferSize = Math.ceil(sizeInBits / 8.0).toInt() + 1
        //
        // NOTICE: This code only works when using play services v. 8.1 or higher.
        //
        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        val byteArray = ByteArray(bufferSize)
        val buffer = ByteBuffer.wrap(byteArray)
        if (!buffer.hasArray() || !buffer.array()!!.contentEquals(byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw IllegalStateException("Failed to create valid buffer for camera source.")
        }
        mBytesToByteBuffer[byteArray] = buffer
        return byteArray
    }

    //==============================================================================================
    // Frame processing
    //==============================================================================================

    /**
     * Called when the camera has a new preview frame.
     */
    private inner class CameraPreviewCallback : Camera.PreviewCallback {
        override fun onPreviewFrame(data: ByteArray, camera: Camera) {
            mFrameProcessor?.setNextFrame(data, camera)
        }
    }

    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera.  This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     * <p/>
     * While detection is running on a frame, new frames may be received from the camera.  As these
     * frames come in, the most recent frame is held onto as pending.  As soon as detection and its
     * associated processing are done for the previous frame, detection on the mostly recently
     * received frame will immediately start on the same thread.
     */
    private inner class FrameProcessingRunnable(private val detector: Detector<*>) : Runnable {
        var mDetector: Detector<*>? = null
        val mStartTimeMillis = SystemClock.elapsedRealtime()

        // This lock guards all of the member variables below.
        private val mLock = Object()
        private var mActive = true

        // These pending variables hold the state associated with the new frame awaiting processing.
        private var mPendingTimeMillis: Long = 0
        private var mPendingFrameId = 0
        private var mPendingFrameData: ByteBuffer? = null

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
         * Sets the frame data received from the camera.  This adds the previous unused frame buffer
         * (if present) back to the camera, and keeps a pending reference to the frame data for
         * future use.
         */
        fun setNextFrame(data: ByteArray, camera: Camera) {
            synchronized(mLock) {
                camera.addCallbackBuffer(mPendingFrameData?.array())
                mPendingFrameData = null
                if (!mBytesToByteBuffer.containsKey(data)) {
                    Log.d(TAG, "Skipping frame.  Could not find ByteBuffer associated with the image " + "data from the camera.")
                    return
                }
                // Timestamp and frame ID are maintained here, which will give downstream code some
                // idea of the timing of frames received and when frames were dropped along the way.
                mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTimeMillis
                mPendingFrameId++
                mPendingFrameData = mBytesToByteBuffer.get(data)
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

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun run() {
            var outputFrame: Frame? = null
            var data: ByteBuffer? = null

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

                    //REDUCE SIZE OF CAMERA PREVIEW
                    val previewW = mPreviewSize?.width
                    val previewH = mPreviewSize?.height
                    //Log.d("ASD", "FRAME SIZE: "+previewW+"x"+previewH);
                    outputFrame = Frame.Builder()
                            .setImageData(quarterNV21(mPendingFrameData!!, previewW!!, previewH!!), previewW.div(4), previewH.div(4), ImageFormat.NV21)
                            .setId(mPendingFrameId)
                            .setTimestampMillis(mPendingTimeMillis)
                            .setRotation(mRotation)
                            .build()

                    // Hold onto the frame data locally, so that we can use this for detection
                    // below.  We need to clear mPendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    data = mPendingFrameData!!
                    mPendingFrameData = null
                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    mDetector?.receiveFrame(outputFrame)
                } catch (t: Throwable) {
                    Log.e(TAG, "Exception thrown from receiver.", t)
                } finally {
                    mCamera?.addCallbackBuffer(data?.array())
                }
            }
        }

    }

    private fun releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            mCamera?.lock()
        }
    }

    private fun checkCamera(): Boolean = mCamera != null
}