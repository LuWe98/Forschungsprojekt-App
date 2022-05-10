package com.serverless.forschungsprojectfaas.view.custom.subsampling

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Default implementation of [com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder]
 * using Android's [android.graphics.BitmapRegionDecoder], based on the Skia library. This
 * works well in most circumstances and has reasonable performance due to the cached decoder instance,
 * however it has some problems with grayscale, indexed and CMYK images.
 *
 * A [ReadWriteLock] is used to delegate responsibility for multi threading behaviour to the
 * [BitmapRegionDecoder] instance on SDK &gt;= 21, whilst allowing this class to block until no
 * tiles are being loaded before recycling the decoder. In practice, [BitmapRegionDecoder] is
 * synchronized internally so this has no real impact on performance.
 */
class SkiaImageRegionDecoder(
    bitmapConfig: Bitmap.Config?
) : ImageRegionDecoder {

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = "$FILE_PREFIX/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }

    private var decoder: BitmapRegionDecoder? = null
    private val decoderLock: ReadWriteLock = ReentrantReadWriteLock(true)
    private val bitmapConfig: Bitmap.Config?

    /**
     * Before SDK 21, BitmapRegionDecoder was not synchronized internally. Any attempt to decode
     * regions from multiple threads with one decoder instance causes a segfault. For old versions
     * use the write lock to enforce single threaded decoding.
     */
    private val decodeLock: Lock
        get() = if (Build.VERSION.SDK_INT < 21) {
            decoderLock.writeLock()
        } else {
            decoderLock.readLock()
        }

    @get:Synchronized
    override val isReady: Boolean
        get() = decoder != null && !decoder!!.isRecycled

    init {
        val globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig()
        this.bitmapConfig = when {
            bitmapConfig != null -> bitmapConfig
            globalBitmapConfig != null -> globalBitmapConfig
            else -> Bitmap.Config.RGB_565
        }
    }

    @Throws(Exception::class)
    override fun init(context: Context, uri: Uri): Point {
        val uriString = uri.toString()
        decoder = when {
            uriString.startsWith(RESOURCE_PREFIX) -> {
                decoderFromResource(context, uri)
            }
            uriString.startsWith(ASSET_PREFIX) -> {
                val assetName = uriString.substring(ASSET_PREFIX.length)
                BitmapRegionDecoder.newInstance(context.assets.open(assetName, AssetManager.ACCESS_RANDOM), false)
            }
            uriString.startsWith(FILE_PREFIX) -> {
                BitmapRegionDecoder.newInstance(uriString.substring(FILE_PREFIX.length), false)
            }
            else -> with(context.contentResolver.openInputStream(uri)) {
                BitmapRegionDecoder.newInstance(this!!, false)
            }
        }
        return Point(decoder!!.width, decoder!!.height)
    }

    private fun decoderFromResource(context: Context, uri: Uri): BitmapRegionDecoder? {
        val res: Resources
        val packageName = uri.authority
        res = if (context.packageName == packageName) {
            context.resources
        } else {
            val pm = context.packageManager
            pm.getResourcesForApplication(packageName!!)
        }
        var id = 0
        val segments = uri.pathSegments
        val size = segments.size
        if (size == 2 && segments[0] == "drawable") {
            val resName = segments[1]
            id = res.getIdentifier(resName, "drawable", packageName)
        } else if (size == 1 && TextUtils.isDigitsOnly(segments[0])) {
            try {
                id = segments[0].toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        return BitmapRegionDecoder.newInstance(context.resources.openRawResource(id), false)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        decodeLock.lock()
        return try {
            if (decoder != null && !decoder!!.isRecycled) {
                val options = BitmapFactory.Options()
                options.inSampleSize = sampleSize
                options.inPreferredConfig = bitmapConfig
                val bitmap =
                    decoder!!.decodeRegion(sRect, options) ?: throw RuntimeException("Skia image decoder returned null bitmap - image format may not be supported")
                bitmap
            } else {
                throw IllegalStateException("Cannot decode region after decoder has been recycled")
            }
        } finally {
            decodeLock.unlock()
        }
    }

    @Synchronized
    override fun recycle() {
        decoderLock.writeLock().lock()
        decoder = try {
            decoder!!.recycle()
            null
        } finally {
            decoderLock.writeLock().unlock()
        }
    }
}