package com.serverless.forschungsprojectfaas.view.fragments.old

import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.serverless.forschungsprojectfaas.databinding.FragmentEmbeddedPreviewBinding
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.utils.io.errors.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class FragmentEmbeddedPreview : BindingFragment<FragmentEmbeddedPreviewBinding>() {


    private val MEDIA_TYPE_IMAGE = 1
    private val MEDIA_TYPE_VIDEO = 2

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCamera()
        binding.button.setOnClickListener {
            mCamera?.takePicture(null, null, mPicture)
        }
    }

    private fun initCamera(){
        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            CameraPreview(requireContext(), it).also { preview ->
                binding.previewContainer.addView(preview)
            }
        }
    }

    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    private val mPicture = Camera.PictureCallback { data, _ ->
        val pictureFile: File = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: return@PictureCallback

        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) { }
    }


    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }


}