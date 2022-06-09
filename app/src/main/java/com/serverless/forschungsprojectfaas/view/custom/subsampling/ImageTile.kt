package com.serverless.forschungsprojectfaas.view.custom.subsampling

import android.graphics.Bitmap
import android.graphics.Rect

data class ImageTile(
    var sRect: Rect? = null,
    var sampleSize: Int = 0,
    var bitmap: Bitmap? = null,
    var loading: Boolean = false,
    var visible: Boolean = false,
    // Volatile fields instantiated once then updated before use to reduce GC.
    var vRect: Rect = Rect(),
    var fileSRect: Rect? = Rect(sRect)
)