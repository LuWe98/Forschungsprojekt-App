package com.serverless.forschungsprojectfaas.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars

class BarBatchDisplay : SubsamplingScaleImageView,
    View.OnTouchListener,
    View.OnClickListener,
    View.OnLongClickListener {

    companion object {
        private const val BASE_RECT_STROKE = 1.5f
        private const val BASE_RECT_ALPHA = 40

        private const val BASE_TEXT_STROKE = 0f
        private const val BASE_TEXT_PADDING = 5f
        private const val BASE_TEXT_SIZE = 15f
        private const val MAX_SCALE = 100f
        private const val UNKNOWN_INDEX = -1

        private val RED = Color.rgb(255, 0, 0)
    }

    private val targetRectF = RectF()
    private val transformationSource = PointF()
    private val transformationTarget = PointF()
    private val lastTouchCoordinates = FloatArray(2)
    private val lastX get() = lastTouchCoordinates[0]
    private val lastY get() = lastTouchCoordinates[1]


    private val batches = mutableListOf<BatchWithBars>()
    private val bars = mutableListOf<Bar>()
    private val selectedBarIds = mutableListOf<String>()

    private val squareFillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val squareStrokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.RIGHT
        strokeWidth = BASE_TEXT_STROKE
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        initialise()
    }

    private fun initialise() {
        maxScale = MAX_SCALE
        setOnTouchListener(this)
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isReady) return
        if (canvas == null) return

        squareStrokePaint.strokeWidth = scale * BASE_RECT_STROKE
        textPaint.textSize = scale * BASE_TEXT_SIZE
        val textPadding = scale * BASE_TEXT_PADDING

        bars.forEach { bar ->
            val batch = batches.first { it.batch.batchId == bar.batchId }.batch
            val color = if (selectedBarIds.contains(bar.barId)) RED else batch.colorInt

            sourceToRectCoordinates(bar.rect)
            squareFillPaint.color = color
            squareFillPaint.alpha = BASE_RECT_ALPHA
            canvas.drawRect(targetRectF, squareFillPaint)
            squareStrokePaint.color = color
            canvas.drawRect(targetRectF, squareStrokePaint)

            textPaint.color = color
            canvas.drawText(batch.caption, targetRectF.right - textPadding, targetRectF.bottom - textPadding, textPaint)
        }
    }

    private fun sourceToRectCoordinates(source: RectF, target: RectF = targetRectF) {
        transformationSource.set(source.left, source.top)
        sourceToViewCoord(transformationSource, transformationTarget)
        val top = transformationTarget.y
        val left = transformationTarget.x
        transformationSource.set(source.right, source.bottom)
        sourceToViewCoord(transformationSource, transformationTarget)
        val right = transformationTarget.x
        val bottom = transformationTarget.y
        target.set(left, top, right, bottom)
    }

    fun setRectangles(batchesWiths: List<BatchWithBars>) {
        batches.clear()
        bars.clear()
        batches.addAll(batchesWiths)
        bars.addAll(batchesWiths.flatMap(BatchWithBars::bars))
        invalidate()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            lastTouchCoordinates[0] = event.x
            lastTouchCoordinates[1] = event.y
        }
        return event?.let(::onTouchEvent) ?: false
    }

    private fun findCanvasItemListPosition(x: Float = lastX, y: Float = lastY): Int? {
        if (x < 0 || y < 0) return null
        val converted = viewToSourceCoord(x, y) ?: return null
        val index = bars.indexOfFirst { entry ->
            entry.rect.contains(converted.x, converted.y)
        }
        return if (index == UNKNOWN_INDEX) null else index
    }

    override fun onClick(v: View?) {
        findCanvasItemListPosition()?.let { index ->
            val bar = bars[index]
            log("BAR CLICKED: $bar")

            if (selectedBarIds.contains(bar.barId)) {
                selectedBarIds.remove(bar.barId)
            } else {
                selectedBarIds.add(bar.barId)
            }
            invalidate()
        } ?: run {
            log("NOT CLICKED ON RECT")
            log("X: $lastX")
            log("Y: $lastY")
        }
    }

    override fun onLongClick(v: View?): Boolean {
        findCanvasItemListPosition()?.let { index ->
            val entry = bars[index]
            log("BAR LONG CLICKED: $entry")
        } ?: run {
            log("NOT LONG CLICKED ON RECT")
            log("X: $lastX")
            log("Y: $lastY")
        }
        return true
    }
}