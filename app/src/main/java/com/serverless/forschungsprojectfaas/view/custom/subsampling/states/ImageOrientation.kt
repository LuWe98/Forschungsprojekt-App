package com.serverless.forschungsprojectfaas.view.custom.subsampling.states

enum class ImageOrientation(
    val degrees: Int
) {
    ORIENTATION_USE_EXIF(-1),
    ORIENTATION_0(0),
    ORIENTATION_90(90),
    ORIENTATION_180(180),
    ORIENTATION_270(270);

    companion object {
        fun fromDegrees(degrees: Int): ImageOrientation? = when(degrees) {
            ORIENTATION_0.degrees -> ORIENTATION_0
            ORIENTATION_90.degrees -> ORIENTATION_90
            ORIENTATION_180.degrees -> ORIENTATION_180
            ORIENTATION_270.degrees -> ORIENTATION_270
            ORIENTATION_USE_EXIF.degrees -> ORIENTATION_USE_EXIF
            else -> null
        }
    }
}