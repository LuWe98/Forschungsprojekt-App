package com.serverless.forschungsprojectfaas.view.fragments.old

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat.getSystemService


@SuppressLint("ViewConstructor")
class CameraPreview(
    context: Context,
    private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback {

    private val TAG = "LOL"

    private var isPreviewRunning = false

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        previewCamera()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (isPreviewRunning) {
            mCamera.stopPreview()
        }

        val parameters: Camera.Parameters = mCamera.parameters
        Log.d("TEST", "PARAM: $parameters")
        val display: Display = getSystemService(context, WindowManager::class.java)!!.defaultDisplay
        Log.d("TEST", "DISPLAY: $display")

        val previewSizes = parameters.supportedPreviewSizes
        Log.d("TEST", "SIZES: $previewSizes")

        var bestIndex = 0
        var bestHeight = Int.MAX_VALUE

        previewSizes.forEachIndexed { index, size ->
            if(height - size.height < bestHeight) {
                bestHeight = size.height
                bestIndex = index
            }
        }

        val newHeight = previewSizes[0].height
        val newWidth = previewSizes[0].width

        if (display.rotation == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90)
        }

        if (display.rotation == Surface.ROTATION_90) {
        }

        if (display.rotation == Surface.ROTATION_180) {
        }

        if (display.rotation == Surface.ROTATION_270) {
        }
        mCamera.setDisplayOrientation(90)
        parameters.setPreviewSize(w, h)
        mCamera.parameters = parameters
        previewCamera()
        isPreviewRunning = true
    }

    /*

        if (mHolder.surface == null) return

        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        mCamera.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
                isPreviewRunning = true
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: ${e.message}")
            }
        }
     */

    private fun previewCamera(){
        mCamera.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: ${e.message}")
            }
        }
    }
}