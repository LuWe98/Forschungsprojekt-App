package com.serverless.forschungsprojectfaas.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withTranslation

class CustomZoom : AppCompatImageView,
    View.OnTouchListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        sharedConstructing(context)
    }

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var mMatrixValues: FloatArray? = null
    private var startPoint = PointF()
    //private var mStart = PointF()

    var mMatrix: Matrix? = null
    var state: ImageState = ImageState.IDLE

    var savedScale = 1f
    var mMinScale = 1f
    var mMaxScale = 100f

    var origWidth = 0f
    var origHeight = 0f
    var viewWidth = 0
    var viewHeight = 0

    enum class ImageState {
        IDLE,
        DRAG,
        ZOOM
    }

    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, scaleListener)
        mMatrix = Matrix()
        mMatrixValues = FloatArray(9)
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX
        mGestureDetector = GestureDetector(context, this)
        setOnTouchListener(this)
    }

    private val scaleListener = object :ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            state = ImageState.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val currentSaveScale = savedScale
            savedScale *= scaleFactor
            if (savedScale > mMaxScale) {
                savedScale = mMaxScale
                scaleFactor = mMaxScale / currentSaveScale
            } else if (savedScale < mMinScale) {
                savedScale = mMinScale
                scaleFactor = mMinScale / currentSaveScale
            }
            if (origWidth * savedScale <= viewWidth || origHeight * savedScale <= viewHeight) {
                mMatrix!!.postScale(scaleFactor, scaleFactor, viewWidth / 2.toFloat(), viewHeight / 2.toFloat())
            } else {
                mMatrix!!.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            }
            fixTranslation()
            return true
        }
    }

    fun fixTranslation() {
        mMatrix!!.getValues(mMatrixValues) //put matrix values into a float array so we can analyze
        val transX = mMatrixValues!![Matrix.MTRANS_X] //get the most recent translation in x direction
        val transY = mMatrixValues!![Matrix.MTRANS_Y] //get the most recent translation in y direction
        val fixTransX = getFixedTranslation(transX, viewWidth.toFloat(), origWidth * savedScale)
        val fixTransY = getFixedTranslation(transY, viewHeight.toFloat(), origHeight * savedScale)

        if (fixTransX != 0f || fixTransY != 0f) {
            mMatrix!!.postTranslate(fixTransX, fixTransY)
        }
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        mScaleDetector?.onTouchEvent(event)
        mGestureDetector?.onTouchEvent(event)
        val currentPoint = PointF(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint.set(currentPoint)
                state = ImageState.DRAG
                //mStart.set(currentPoint)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                state = ImageState.IDLE
            }
            MotionEvent.ACTION_MOVE -> if (state == ImageState.DRAG) {
                val dx = currentPoint.x - startPoint.x
                val dy = currentPoint.y - startPoint.y
                val fixTransX = getFixedDragTransition(dx, viewWidth.toFloat(), origWidth * savedScale)
                val fixTransY = getFixedDragTransition(dy, viewHeight.toFloat(), origHeight * savedScale)
                mMatrix!!.postTranslate(fixTransX, fixTransY)

                fixTranslation()
                startPoint[currentPoint.x] = currentPoint.y
            }
        }
        imageMatrix = mMatrix
        return false
    }

    private fun getFixedTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) { // case: NOT ZOOMED
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else { //CASE: ZOOMED
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) { // negative x or y translation (down or to the right)
            return -trans + minTrans
        }
        if (trans > maxTrans) { // positive x or y translation (up or to the left)
            return -trans + maxTrans
        }
        return 0F
    }

    private fun getFixedDragTransition(delta: Float, viewSize: Float, contentSize: Float): Float = if (contentSize <= viewSize) 0F else delta

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (savedScale == 1f) {
            rescaleToOriginalSize()
        }
    }

    private fun rescaleToOriginalSize() {
        savedScale = 1f
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) return

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()
        val scale: Float = scaleX.coerceAtMost(scaleY)
        mMatrix!!.setScale(scale, scale)

        val redundantYSpace = (viewHeight.toFloat() - scale * imageHeight.toFloat()) / 2f
        val redundantXSpace = (viewWidth.toFloat() - scale * imageWidth.toFloat()) / 2f
        mMatrix!!.postTranslate(redundantXSpace, redundantYSpace)
        origWidth = viewWidth - 2f * redundantXSpace
        origHeight = viewHeight - 2f * redundantYSpace
        imageMatrix = mMatrix
    }

    private val paint = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //canvas?.drawCircle(550f, 500f, 100f, paint)
        //canvas?.scale(savedScale, savedScale)
    }

    override fun onLongPress(motionEvent: MotionEvent) {}
    override fun onShowPress(motionEvent: MotionEvent) {}
    override fun onDown(motionEvent: MotionEvent) = false
    override fun onSingleTapUp(motionEvent: MotionEvent) = false
    override fun onScroll(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float) = false
    override fun onFling(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float) = false
    override fun onSingleTapConfirmed(motionEvent: MotionEvent) = false
    override fun onDoubleTapEvent(motionEvent: MotionEvent) = false
    override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
        rescaleToOriginalSize()
        return false
    }
}