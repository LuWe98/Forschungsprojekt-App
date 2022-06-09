package com.serverless.forschungsprojectfaas.extensions

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.BoxDimensions
import kotlin.math.max
import kotlin.math.min

fun Rect.containsPartial(other: Rect): Boolean = left < right
        && top < bottom // now check for containment
        && ((other.top in top..bottom) || (other.bottom in top..bottom))
        && ((other.left in left..right) || (other.right in left..right))

fun RectF.containsPartial(other: RectF): Boolean = left < right
        && top < bottom // now check for containment
        && ((other.top in top..bottom) || (other.bottom in top..bottom))
        && ((other.left in left..right) || (other.right in left..right))

val List<RectF>.averageRectDimension
    get(): BoxDimensions = BoxDimensions(
        width = sumOf(RectF::width) / size,
        height = sumOf(RectF::height) / size
    )

val RectF.area get() = width() * height()

val RectF.center get(): PointF = PointF(centerX(), centerY())

fun RectF?.intersectingArea(other: RectF?): Float {
    if(this == null || other == null) return 0f
    val dx = min(right, other.right) - max(left, other.left)
    val dy = min(bottom, other.bottom) - max(top, other.top)
    if(dx < 0f || dy < 0f) return 0f
    return dx * dy
}

fun RectF?.intersectingRect(other: RectF?): RectF? {
    if(this == null || other == null) return null
    val minRight = min(right, other.right)
    val maxLeft = max(left, other.left)
    val minBottom = min(bottom, other.bottom)
    val maxTop = max(top, other.top)
    if(minRight - maxLeft < 0f || minBottom - maxTop < 0f) return null
    return RectF(maxLeft, maxTop, minRight, minBottom)
}