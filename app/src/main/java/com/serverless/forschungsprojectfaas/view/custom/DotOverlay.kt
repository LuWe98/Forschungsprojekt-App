package com.serverless.forschungsprojectfaas.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.model.room.entities.BarBatch
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.junctions.BarBatchWithBars

class DotOverlay : SubsamplingScaleImageView,
    View.OnTouchListener,
    View.OnClickListener,
    View.OnLongClickListener {

    private val vRectTest = RectF()
    private val transformationSource = PointF()
    private val transformationTarget = PointF()

    private val currentAggregates = mutableListOf<BarBatchWithBars>()
    private val currentStickEntries = mutableListOf<Bar>()

    private val selectedPositions = mutableListOf<Int>()
    private val lastTouchCoordinates = FloatArray(2)

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        initialise()
    }

    private fun initialise() {
        maxScale = 100f
        setOnTouchListener(this)
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isReady) return
        paint.strokeWidth = scale * 2.5f
        currentStickEntries.forEachIndexed { index, entry ->
            sourceToRectCoord(entry.rect, vRectTest)
            if(selectedPositions.contains(index)) {
                paint.color = Color.rgb(255, 0, 0)
            } else {
                paint.color = currentAggregates.first { it.barBatch.id == entry.batchId }.barBatch.colorInt
            }
            canvas?.drawRect(vRectTest, paint)
        }
    }

    private fun sourceToRectCoord(source: RectF, target: RectF) {
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

    fun setRectangles(batchesWithBars: List<BarBatchWithBars>) {
        currentAggregates.clear()
        currentStickEntries.clear()
        currentAggregates.addAll(batchesWithBars)
        currentStickEntries.addAll(batchesWithBars.flatMap(BarBatchWithBars::bars))
        invalidate()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN) {
            lastTouchCoordinates[0] = event.x
            lastTouchCoordinates[1] = event.y
        }
        return event?.let(::onTouchEvent) ?: false
    }

    private fun findContainingRectListPosition(xCoord: Float = lastTouchCoordinates[0], yCoord: Float = lastTouchCoordinates[1]): Int? {
        if(xCoord == -1f|| yCoord == -1f) return null
        val converted = viewToSourceCoord(xCoord, yCoord) ?: return null
        val index = currentStickEntries.indexOfFirst { entry ->
            entry.rect.contains(converted.x, converted.y)
        }
        lastTouchCoordinates[0] = -1f
        lastTouchCoordinates[1] = -1f
        return if(index == -1) null else index
    }

    override fun onClick(v: View?) {
        findContainingRectListPosition()?.let { index ->
            val entry = currentStickEntries[index]
            log("BAR CLICKED: $entry")

            if(selectedPositions.contains(index)) {
                selectedPositions.remove(index)
            } else {
                selectedPositions.add(index)
            }
            invalidate()
        }
    }

    override fun onLongClick(v: View?): Boolean {
        findContainingRectListPosition()?.let { index ->
            val entry = currentStickEntries[index]
            log("BAR LONG CLICKED: $entry")
        }
        return true
    }
}