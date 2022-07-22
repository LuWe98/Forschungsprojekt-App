package com.serverless.forschungsprojectfaas.model.ktor

import android.graphics.PointF
import android.graphics.RectF
import kotlinx.serialization.Serializable

@Serializable
data class ImageInformationBar(
    val caption: String,
    val bottom: Float,
    val left: Float,
    val right: Float,
    val top: Float
) {
    val rect get() = RectF(left, top, right, bottom)
    val height get() = bottom - top
    val width get() = right - left
    val centerX get() = right - width/2f
    val centerY get() = bottom - height/2f
    val center get() = PointF(centerX, centerY)
}