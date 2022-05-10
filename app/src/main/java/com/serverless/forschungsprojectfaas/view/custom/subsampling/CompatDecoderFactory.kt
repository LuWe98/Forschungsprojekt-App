package com.serverless.forschungsprojectfaas.view.custom.subsampling

import kotlin.jvm.JvmOverloads
import android.graphics.Bitmap
import java.lang.reflect.InvocationTargetException
import kotlin.Throws

/**
 * Compatibility factory to instantiate decoders with empty public constructors.
 * @param <T> The base type of the decoder this factory will produce.
</T> */
/**
 * Construct a factory for the given class. This must have a constructor that accepts a [Bitmap.Config] instance.
 * @param clazz a class that implements [ImageDecoder] or [ImageRegionDecoder].
 * @param bitmapConfig bitmap configuration to be used when loading images.
 */
class CompatDecoderFactory<T> @JvmOverloads constructor(
    private val clazz: Class<out T>,
    private val bitmapConfig: Bitmap.Config? = null
) : DecoderFactory<T> {

    /**
     * Construct a factory for the given class. This must have a default constructor.
     * @param clazz a class that implements [ImageDecoder] or [ImageRegionDecoder].
     */
    @Throws(IllegalAccessException::class, InstantiationException::class, NoSuchMethodException::class, InvocationTargetException::class)
    override fun make(): T = if (bitmapConfig == null) {
        clazz.newInstance()
    } else {
        val ctor = clazz.getConstructor(Bitmap.Config::class.java)
        ctor.newInstance(bitmapConfig)
    }
}