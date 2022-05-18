package com.serverless.forschungsprojectfaas.model.ktor

import android.graphics.PointF
import android.graphics.RectF
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.absoluteValue

@Serializable
data class ProcessedBox(
    val caption: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val rect get() = RectF(left, top, right, bottom)
    val height get() = bottom - top
    val width get() = right - left
    val centerX get() = right - width/2f
    val centerY get() = bottom - height/2f
    val center get() = PointF(centerX, centerY)

    fun getMiddleXCoordinate(other: ProcessedBox): Float {
        val diff = (centerX - other.centerX).absoluteValue / 2f
        return if(centerX < other.centerX) centerX + diff else other.centerX + diff
    }
}
