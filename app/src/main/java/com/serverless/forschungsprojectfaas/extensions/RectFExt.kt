package com.serverless.forschungsprojectfaas.extensions

import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.BoxDimension

val List<RectF>.averageRectDimension
    get(): BoxDimension = BoxDimension(
        width = sumOf(RectF::width) / size,
        height = sumOf(RectF::height) / size
    )
