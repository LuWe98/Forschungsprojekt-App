package com.serverless.forschungsprojectfaas.view.fragments.old

import android.view.View
import com.serverless.forschungsprojectfaas.databinding.FragmentCameraXBinding
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import java.util.concurrent.ExecutorService

import android.os.Build
import com.serverless.forschungsprojectfaas.extensions.askForPermissions
import com.serverless.forschungsprojectfaas.extensions.isPermissionGranted

//https://developer.android.com/codelabs/camerax-getting-started#2
@AndroidEntryPoint
class FragmentCameraX : BindingFragment<FragmentCameraXBinding>() {

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            askForPermissions.launch(REQUIRED_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        binding.button.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private val askForPermissions = askForPermissions {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Not all granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all(::isPermissionGranted)


    private fun takePhoto() {

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewContainer.surfaceProvider)
            }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}