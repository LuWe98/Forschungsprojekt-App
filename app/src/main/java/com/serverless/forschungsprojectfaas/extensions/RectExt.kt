package com.serverless.forschungsprojectfaas.extensions

import android.graphics.Rect
import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.BoxDimension

fun Rect.containsPartial(r: Rect): Boolean = left < right && top < bottom // now check for containment
        && ((r.top in top..bottom) || (r.bottom in top..bottom))
        && ((r.left in left..right) || (r.right in left..right))

fun Rect.containsPartial(r: RectF): Boolean = left < right
        && top < bottom // now check for containment
        && ((r.top >= top && r.top <= bottom) || (r.bottom >= top && r.bottom <= bottom))
        && ((r.left >= left && r.left <= right) || (r.right >= left && r.right <= right))

val List<RectF>.averageRectDimension
    get(): BoxDimension = BoxDimension(
        width = sumOf(RectF::width) / size,
        height = sumOf(RectF::height) / size
    )

fun RectF.setLeft(left: Float): RectF = apply {
    set(left, top, right, bottom)
}
fun RectF.setTop(top: Float): RectF = apply {
    set(left, top, right, bottom)
}
fun RectF.setRight(right: Float): RectF = apply {
    set(left, top, right, bottom)
}
fun RectF.setBottom(bottom: Float): RectF = apply {
    set(left, top, right, bottom)
}
