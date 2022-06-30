package com.serverless.forschungsprojectfaas.model.ktor

import android.graphics.PointF
import android.graphics.RectF
import com.serverless.forschungsprojectfaas.extensions.BatchId
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@Serializable
data class PotentialBox(
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

    fun asBar(batchId: BatchId, pileId: String) = Bar(
        batchId = batchId,
        pileId = pileId,
        rect = rect
    )
}