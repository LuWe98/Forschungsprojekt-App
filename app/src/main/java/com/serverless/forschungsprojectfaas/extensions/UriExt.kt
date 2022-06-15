package com.serverless.forschungsprojectfaas.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface

fun Uri.toExifDegrees(context: Context): Int {
    val exif = if (Build.VERSION.SDK_INT > 23) {
        ExifInterface(context.contentResolver.openInputStream(this) ?: return 0)
    } else {
        ExifInterface(path ?: return 0)
    }

    return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

fun Uri.loadBitmap(context: Context): Bitmap {
    val inputStream = context.contentResolver.openInputStream(this)
    return BitmapFactory.decodeStream(inputStream)
}