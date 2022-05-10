package com.serverless.forschungsprojectfaas.view.custom.subsampling

import android.graphics.Bitmap
import androidx.annotation.Keep
import kotlin.Throws
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.text.TextUtils
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.InputStream
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.RuntimeException

/**
 * Default implementation of [com.davemorrissey.labs.subscaleview.decoder.ImageDecoder]
 * using Android's [android.graphics.BitmapFactory], based on the Skia library. This
 * works well in most circumstances and has reasonable performance, however it has some problems
 * with grayscale, indexed and CMYK images.
 */
class SkiaImageDecoder(bitmapConfig: Bitmap.Config? = null) : ImageDecoder {

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = "$FILE_PREFIX/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }

    private val bitmapConfig: Bitmap.Config?

    init {
        val globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig()
        this.bitmapConfig = when {
            bitmapConfig != null -> bitmapConfig
            globalBitmapConfig != null -> globalBitmapConfig
            else -> Bitmap.Config.RGB_565
        }
    }

    @Throws(Exception::class)
    override fun decode(context: Context, uri: Uri): Bitmap {
        val uriString = uri.toString()
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = bitmapConfig
        }
        val bitmap: Bitmap = when {
            uriString.startsWith(RESOURCE_PREFIX) -> resourceToBitmap(context, uri, options)
            uriString.startsWith(ASSET_PREFIX) -> assetToBitmap(context, uri, options)
            uriString.startsWith(FILE_PREFIX) -> BitmapFactory.decodeFile(uriString.substring(FILE_PREFIX.length), options)
            else -> with(context.contentResolver.openInputStream(uri)) {
                BitmapFactory.decodeStream(this, null, options)!!
            }
        } ?: throw RuntimeException("Skia image region decoder returned null bitmap - image format may not be supported")

        return bitmap
    }

    private fun resourceToBitmap(context: Context, uri: Uri, options: BitmapFactory.Options): Bitmap {
        val packageName = uri.authority
        val res: Resources = if (context.packageName == packageName) {
            context.resources
        } else {
            context.packageManager.getResourcesForApplication(packageName!!)
        }
        var id = 0
        val segments = uri.pathSegments
        val size = segments.size
        if (size == 2 && segments[0] == "drawable") {
            id = res.getIdentifier(segments[1], "drawable", packageName)
        } else if (size == 1 && TextUtils.isDigitsOnly(segments[0])) {
            runCatching {
                id = segments[0].toInt()
            }
        }
        return BitmapFactory.decodeResource(context.resources, id, options)
    }

    private fun assetToBitmap(context: Context, uri: Uri, options: BitmapFactory.Options): Bitmap? {
        val assetName = uri.toString().substring(ASSET_PREFIX.length)
        return BitmapFactory.decodeStream(
            context.assets.open(assetName),
            null,
            options
        )
    }
}