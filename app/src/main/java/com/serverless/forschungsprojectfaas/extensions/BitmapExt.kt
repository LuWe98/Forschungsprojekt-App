package com.serverless.forschungsprojectfaas.extensions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun Bitmap.rotate(degree: Int) : Bitmap {
    if(degree == 0) return this
    val matrix = Matrix().apply { postRotate(degree.toFloat()) }
    val rotatedBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return rotatedBitmap
}

fun Bitmap.saveToInternalStorage(
    context: Context,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    fileSuffix: String = format.name.lowercase(),
    quality: Int = 100,
    dir: String = "Images",
    imageName: String = System.currentTimeMillis().toString()
): Uri {
    val fileName = "$imageName.$fileSuffix"
    val file = File(ContextWrapper(context).getDir(dir, Context.MODE_PRIVATE), fileName)
    FileOutputStream(file).let { stream ->
        compress(format, quality, stream)
        stream.flush()
        stream.close()
    }
    return Uri.parse(file.absolutePath)
}