package com.serverless.forschungsprojectfaas.extensions

import android.content.res.Resources

val Number.px get() = (toFloat() / Resources.getSystem().displayMetrics.density).toInt()

val Number.dp get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()

val Int.toHex get(): String = Integer.toHexString(this)

inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun Float.between(from: Float, to: Float): Boolean = this in from..to