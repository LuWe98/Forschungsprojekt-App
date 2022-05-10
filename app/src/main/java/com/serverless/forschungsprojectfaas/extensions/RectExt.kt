package com.serverless.forschungsprojectfaas.extensions

import android.graphics.Rect
import android.graphics.RectF

fun Rect.containsPartial(r: Rect): Boolean = left < right && top < bottom // now check for containment
        && ((r.top in top..bottom) || (r.bottom in top..bottom))
        && ((r.left in left..right) || (r.right in left..right))

fun Rect.containsPartial(r: RectF): Boolean = left < right
        && top < bottom // now check for containment
        && ((r.top >= top && r.top <= bottom) || (r.bottom >= top && r.bottom <= bottom))
        && ((r.left >= left && r.left <= right) || (r.right >= left && r.right <= right))