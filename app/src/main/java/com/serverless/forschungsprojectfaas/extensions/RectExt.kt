package com.serverless.forschungsprojectfaas.extensions

import android.graphics.PointF
import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.BoxDimensions
import kotlin.math.max
import kotlin.math.min

val RectF.area get() = width() * height()

val RectF.center get(): PointF = PointF(centerX(), centerY())

fun RectF.isIntersecting(other: RectF): Boolean = intersectingArea(other) > 0f

fun RectF?.intersectingArea(other: RectF?): Float {
    if(this == null || other == null) return 0f
    val dx = min(right, other.right) - max(left, other.left)
    val dy = min(bottom, other.bottom) - max(top, other.top)
    return if(dx <= 0f || dy <= 0f) 0f else dx * dy
}

fun RectF?.intersectingRect(other: RectF?): RectF? {
    if(this == null || other == null) return null
    val minRight = min(right, other.right)
    val maxLeft = max(left, other.left)
    val minBottom = min(bottom, other.bottom)
    val maxTop = max(top, other.top)
    return if(minRight - maxLeft <= 0f || minBottom - maxTop <= 0f) null else RectF(maxLeft, maxTop, minRight, minBottom)
}