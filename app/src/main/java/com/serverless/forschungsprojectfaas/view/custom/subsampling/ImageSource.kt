package com.serverless.forschungsprojectfaas.view.custom.subsampling

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Helper class used to set the source and additional attributes from a variety of sources. Supports
 * use of a bitmap, asset, resource, external file or any other URI.
 *
 * When you are using a preview image, you must set the dimensions of the full size image on the
 * ImageSource object for the full size image using the [.dimensions] method.
 */
class ImageSource {
    val uri: Uri?
    val bitmap: Bitmap?
    val resource: Int?
    var tile: Boolean
        private set
    var sWidth = 0
        private set
    var sHeight = 0
        private set
    var sRegion: Rect? = null
        private set
    var isCached = false
        private set

    private constructor(bitmap: Bitmap, cached: Boolean) {
        this.bitmap = bitmap
        uri = null
        resource = null
        tile = false
        sWidth = bitmap.width
        sHeight = bitmap.height
        isCached = cached
    }

    private constructor(parsedUri: Uri) {
        var uri = parsedUri
        val uriString = uri.toString()

        if (uriString.startsWith(FILE_SCHEME)) {
            val uriFile = File(uriString.substring(FILE_SCHEME.length - 1))
            if (!uriFile.exists()) {
                runCatching {
                    uri = Uri.parse(URLDecoder.decode(uriString, "UTF-8"))
                }
            }
        }

        bitmap = null
        resource = null
        tile = true
        this.uri = uri
    }

    private constructor(resource: Int) {
        bitmap = null
        uri = null
        tile = true
        this.resource = resource
    }

    /**
     * Enable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap.,
     * and tiling cannot be disabled when displaying a region of the source image.
     * @return this instance for chaining.
     */
    fun tilingEnabled(): ImageSource = tiling(true)

    /**
     * Disable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap,
     * and tiling cannot be disabled when displaying a region of the source image.
     * @return this instance for chaining.
     */
    fun tilingDisabled(): ImageSource = tiling(false)

    /**
     * Enable or disable tiling of the image. This does not apply to preview images which are always loaded as a single bitmap,
     * and tiling cannot be disabled when displaying a region of the source image.
     * @param tile whether tiling should be enabled.
     * @return this instance for chaining.
     */
    fun tiling(tile: Boolean): ImageSource {
        this.tile = tile
        return this
    }

    /**
     * Use a region of the source image. Region must be set independently for the full size image and the preview if
     * you are using one.
     * @param sRegion the region of the source image to be displayed.
     * @return this instance for chaining.
     */
    fun region(sRegion: Rect?): ImageSource {
        this.sRegion = sRegion
        setInvariants()
        return this
    }

    /**
     * Declare the dimensions of the image. This is only required for a full size image, when you are specifying a URI
     * and also a preview image. When displaying a bitmap object, or not using a preview, you do not need to declare
     * the image dimensions. Note if the declared dimensions are found to be incorrect, the view will reset.
     * @param sWidth width of the source image.
     * @param sHeight height of the source image.
     * @return this instance for chaining.
     */
    fun dimensions(sWidth: Int, sHeight: Int): ImageSource {
        if (bitmap == null) {
            this.sWidth = sWidth
            this.sHeight = sHeight
        }
        setInvariants()
        return this
    }

    fun createBitmap(): Bitmap = Bitmap.createBitmap(
        bitmap!!,
        sRegion!!.left,
        sRegion!!.top,
        sRegion!!.width(),
        sRegion!!.height()
    )

    private fun setInvariants() {
        if (sRegion == null) return
        tile = true
        sWidth = sRegion!!.width()
        sHeight = sRegion!!.height()
    }



    companion object {
        const val FILE_SCHEME = "file:///"
        const val ASSET_SCHEME = "file:///android_asset/"

        /**
         * Create an instance from a resource. The correct resource for the device screen resolution will be used.
         * @param resId resource ID.
         * @return an [ImageSource] instance.
         */
        @JvmStatic
        fun resource(resId: Int) = ImageSource(resId)

        /**
         * Create an instance from an asset name.
         * @param assetName asset name.
         * @return an [ImageSource] instance.
         */
        @JvmStatic
        fun asset(assetName: String) = uri(ASSET_SCHEME + assetName)

        /**
         * Create an instance from a URI. If the URI does not start with a scheme, it's assumed to be the URI
         * of a file.
         * @param uri image URI.
         * @return an [ImageSource] instance.
         */
        fun uri(uri: String): ImageSource {
            var updatedUri = uri
            if (!updatedUri.contains("://")) {
                if (updatedUri.startsWith("/")) {
                    updatedUri = updatedUri.substring(1)
                }
                updatedUri = FILE_SCHEME + updatedUri
            }
            return ImageSource(Uri.parse(updatedUri))
        }

        /**
         * Create an instance from a URI.
         * @param uri image URI.
         * @return an [ImageSource] instance.
         */
        fun uri(uri: Uri) = ImageSource(uri)

        /**
         * Provide a loaded bitmap for display.
         * @param bitmap bitmap to be displayed.
         * @return an [ImageSource] instance.
         */
        fun bitmap(bitmap: Bitmap) = ImageSource(bitmap, false)

        /**
         * Provide a loaded and cached bitmap for display. This bitmap will not be recycled when it is no
         * longer needed. Use this method if you loaded the bitmap with an image loader such as Picasso
         * or Volley.
         * @param bitmap bitmap to be displayed.
         * @return an [ImageSource] instance.
         */
        fun cachedBitmap(bitmap: Bitmap) = ImageSource(bitmap, true)
    }
}