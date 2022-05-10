package com.serverless.forschungsprojectfaas.view.custom.subsampling

import android.graphics.PointF
import com.serverless.forschungsprojectfaas.view.custom.subsampling.states.ImageOrientation
import java.io.Serializable

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
data class ImageViewState(
    val scale: Float,
    val center: PointF,
    val orientation: ImageOrientation
) : Serializable {
    val centerX get(): Float = center.x
    val centerY get(): Float = center.y
}