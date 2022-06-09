package com.serverless.forschungsprojectfaas.model

import kotlin.math.pow
import kotlin.math.sqrt

data class BoxDimensions(
    val width: Float,
    val height: Float
) {
    val diagonal get() = sqrt(width.pow(2) + height.pow(2))
}
