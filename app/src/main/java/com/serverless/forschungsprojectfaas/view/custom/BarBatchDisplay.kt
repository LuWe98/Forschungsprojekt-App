package com.serverless.forschungsprojectfaas.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.serverless.forschungsprojectfaas.extensions.findBarsInsideBounds
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars
import com.serverless.forschungsprojectfaas.utils.Constants
import kotlin.math.max

class BarBatchDisplay : SubsamplingScaleImageView,
    View.OnTouchListener,
    View.OnClickListener,
    View.OnLongClickListener {

    companion object {
        const val DEFAULT_BOX_ALPHA = 40
        const val DEFAULT_BOX_STROKE = 15
        private const val BASE_TEXT_PADDING = 5f
        private const val BASE_TEXT_SIZE = 15f
        private const val ALPHA_COLOR_SWITCH_THRESHOLD = 125

        private const val MAX_SCALE = 100f
        private const val MIN_STROKE = 0.000_000_1f
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) { initialise() }

    private fun initialise() {
        maxScale = MAX_SCALE
        setOnTouchListener(this)
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    private val targetRect = Rect()
    private val transformationSource = PointF()
    private val transformationTarget = PointF()
    private val lastClickCoordinates = PointF()


    private var boxPaintAlpha = DEFAULT_BOX_ALPHA
    private var boxPaintStroke = DEFAULT_BOX_STROKE / 10f

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
        strokeWidth = 0f
    }


    private val batches = mutableListOf<BatchWithBars>()
    private val bars = mutableListOf<Bar>()
    private var draggingBar: Bar? = null

    var isBarSelected: ((Bar) -> (Boolean))? = null

    var onCanvasClicked: ((Bar?, PointF) -> (Unit))? = null

    var onCanvasLongClicked: ((Bar?, PointF) -> (Unit))? = null

    var onBoxDragReleased: ((Bar) -> (Unit))? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isReady) return

        squareStrokePaint.strokeWidth = scale * boxPaintStroke
        textPaint.textSize = scale * BASE_TEXT_SIZE
        val textPadding = scale * BASE_TEXT_PADDING

        visibleFileRect(targetRect)

        bars.findBarsInsideBounds(targetRect.toRectF()).forEach { bar ->
            val batch: Batch? = batches.firstOrNull { it.batch?.batchId == bar.batchId }?.batch
            val color = if (isBarSelected?.invoke(bar) == true) Color.RED else batch?.colorInt ?: Constants.UNASSIGNED_BAR_COLOR
            //val fontColor = if (boxPaintAlpha > ALPHA_COLOR_SWITCH_THRESHOLD) Color.WHITE else color

            sourceToRectCoordinates(bar.rect)

            squareFillPaint.color = color
            squareFillPaint.alpha = boxPaintAlpha
            canvas.drawRect(targetRect, squareFillPaint)

            squareStrokePaint.color = color
            canvas.drawRect(targetRect, squareStrokePaint)

            textPaint.color = Color.WHITE
            canvas.drawText(
                batch?.caption ?: Constants.UNASSIGNED_BAR_CAPTION,
                targetRect.right - textPadding,
                targetRect.bottom - textPadding, textPaint
            )
        }
    }

    fun setBoxes(batchesWiths: List<BatchWithBars>) {
        batches.clear()
        bars.clear()
        batches.addAll(batchesWiths)
        bars.addAll(batchesWiths.flatMap(BatchWithBars::bars))
        invalidate()
    }

    fun setBoxAlpha(alpha: Int) {
        boxPaintAlpha = alpha
        invalidate()
    }

    fun setBoxStroke(stroke: Int) {
        boxPaintStroke = max(stroke / 10f, MIN_STROKE)
        invalidate()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> viewToSourceCoord(event.x, event.y)?.let(lastClickCoordinates::set)
            MotionEvent.ACTION_MOVE -> {
                draggingBar?.let { bar ->
                    viewToSourceCoord(event.x, event.y)?.let { newPoint ->
                        bar.rect.set(
                            newPoint.x - (bar.width / 2),
                            newPoint.y - (bar.height / 2),
                            newPoint.x + (bar.width / 2),
                            newPoint.y + (bar.height / 2)
                        )
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // HANDLE DRAG RELEASE
                draggingBar?.let {
                    onBoxDragReleased?.invoke(it)
                    draggingBar = null
                }
            }
        }

        return event?.let(::onTouchEvent) ?: false
    }

    private fun findCanvasBar(x: Float = lastClickCoordinates.x, y: Float = lastClickCoordinates.y): Bar? {
        return if (x < 0f || y < 0f) null else bars.firstOrNull { it.rect.contains(x, y) }
    }

    override fun onClick(v: View?) {
        onCanvasClicked?.invoke(findCanvasBar(), lastClickCoordinates)
    }

    override fun onLongClick(v: View?): Boolean {
        findCanvasBar()?.let {
            draggingBar = it
        } ?: onCanvasLongClicked?.invoke(null, lastClickCoordinates)
        return true
    }

    private fun sourceToRectCoordinates(source: RectF, target: Rect = targetRect) {
        transformationSource.set(source.left, source.top)
        sourceToViewCoord(transformationSource, transformationTarget)
        val top = transformationTarget.y
        val left = transformationTarget.x
        transformationSource.set(source.right, source.bottom)
        sourceToViewCoord(transformationSource, transformationTarget)
        val right = transformationTarget.x
        val bottom = transformationTarget.y
        target.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}