package com.krtechnologies.officemate

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.vision.face.Face
import android.R.attr.y
import android.R.attr.x
import android.graphics.*
import com.google.android.gms.vision.face.Landmark


/**
 * Created by ingizly on 9/10/18
 **/
class FaceGraphic(private val overlay: GraphicOverlay, private val context: Context) : GraphicOverlay.Graphic(overlay) {


    private var marker: Bitmap? = null

    private var opt: BitmapFactory.Options? = null
    private var resources: Resources? = null

    private var faceId: Int = 0
    var facePosition: PointF? = null
    var faceWidth: Float = 0.toFloat()
    var faceHeight: Float = 0.toFloat()
    var faceCenter: PointF? = null
    var isSmilingProbability = -1f
    var eyeRightOpenProbability = -1f
    var eyeLeftOpenProbability = -1f
    var eulerZ: Float = 0.toFloat()
    var eulerY: Float = 0.toFloat()
    var leftEyePos: PointF? = null
    var rightEyePos: PointF? = null
    var noseBasePos: PointF? = null
    var leftMouthCorner: PointF? = null
    var rightMouthCorner: PointF? = null
    var mouthBase: PointF? = null
    var leftEar: PointF? = null
    var rightEar: PointF? = null
    var leftEarTip: PointF? = null
    var rightEarTip: PointF? = null
    var leftCheek: PointF? = null
    var rightCheek: PointF? = null

    @Volatile
    private var mFace: Face? = null

    init {
        opt = BitmapFactory.Options()
        opt?.inScaled = false
        resources = context.resources
        marker = BitmapFactory.decodeResource(resources, R.drawable.marker, opt)
    }

    fun setId(id: Int) {
        faceId = id
    }

    fun getSmilingProbability(): Float {
        return isSmilingProbability
    }

    fun geteyeRightOpenProbability(): Float {
        return eyeRightOpenProbability
    }

    fun geteyeLeftOpenProbability(): Float {
        return eyeLeftOpenProbability
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateFace(face: Face) {
        mFace = face
        postInvalidate()
    }

    fun goneFace() {
        mFace = null
    }

    override fun draw(canvas: Canvas) {
        val face = mFace
        if (face == null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            isSmilingProbability = -1f
            eyeRightOpenProbability = -1f
            eyeLeftOpenProbability = -1f
            return
        }

        facePosition = PointF(translateX(face.position.x), translateY(face.position.y))
        faceWidth = face.width * 4
        faceHeight = face.height * 4
        faceCenter = PointF(translateX(face.position.x + faceWidth / 8), translateY(face.position.y + faceHeight / 8))
        isSmilingProbability = face.isSmilingProbability
        eyeRightOpenProbability = face.isRightEyeOpenProbability
        eyeLeftOpenProbability = face.isLeftEyeOpenProbability
        eulerY = face.eulerY
        eulerZ = face.eulerZ
        //DO NOT SET TO NULL THE NON EXISTENT LANDMARKS. USE OLDER ONES INSTEAD.
        for (landmark in face.landmarks) {
            when (landmark.type) {
                Landmark.LEFT_EYE -> leftEyePos = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_EYE -> rightEyePos = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.NOSE_BASE -> noseBasePos = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_MOUTH -> leftMouthCorner = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_MOUTH -> rightMouthCorner = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.BOTTOM_MOUTH -> mouthBase = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_EAR -> leftEar = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_EAR -> rightEar = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_EAR_TIP -> leftEarTip = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_EAR_TIP -> rightEarTip = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.LEFT_CHEEK -> leftCheek = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
                Landmark.RIGHT_CHEEK -> rightCheek = PointF(translateX(landmark.position.x), translateY(landmark.position.y))
            }
        }

        val mPaint = Paint()
        mPaint.color = Color.WHITE
        mPaint.strokeWidth = 4f
        if (faceCenter != null)
            canvas.drawBitmap(marker, faceCenter?.x!!, faceCenter?.y!!, null)
        if (noseBasePos != null)
            canvas.drawBitmap(marker, noseBasePos?.x!!, noseBasePos?.y!!, null)
        if (leftEyePos != null)
            canvas.drawBitmap(marker, leftEyePos?.x!!, leftEyePos?.y!!, null)
        if (rightEyePos != null)
            canvas.drawBitmap(marker, rightEyePos?.x!!, rightEyePos?.y!!, null)
        if (mouthBase != null)
            canvas.drawBitmap(marker, mouthBase?.x!!, mouthBase?.y!!, null)
        if (leftMouthCorner != null)
            canvas.drawBitmap(marker, leftMouthCorner?.x!!, leftMouthCorner?.y!!, null)
        if (rightMouthCorner != null)
            canvas.drawBitmap(marker, rightMouthCorner?.x!!, rightMouthCorner?.y!!, null)
        if (leftEar != null)
            canvas.drawBitmap(marker, leftEar?.x!!, leftEar?.y!!, null)
        if (rightEar != null)
            canvas.drawBitmap(marker, rightEar?.x!!, rightEar?.y!!, null)
        if (leftEarTip != null)
            canvas.drawBitmap(marker, leftEarTip?.x!!, leftEarTip?.y!!, null)
        if (rightEarTip != null)
            canvas.drawBitmap(marker, rightEarTip?.x!!, rightEarTip?.y!!, null)
        if (leftCheek != null)
            canvas.drawBitmap(marker, leftCheek?.x!!, leftCheek?.y!!, null)
        if (rightCheek != null)
            canvas.drawBitmap(marker, rightCheek?.x!!, rightCheek?.y!!, null)
    }
}