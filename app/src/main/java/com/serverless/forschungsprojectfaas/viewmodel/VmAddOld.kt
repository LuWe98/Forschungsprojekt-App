package com.serverless.forschungsprojectfaas.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.extensions.app
import com.serverless.forschungsprojectfaas.extensions.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import javax.inject.Inject


@HiltViewModel
class VmAddOld @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val fragmentMainEventChannel = Channel<FragmentMainEvent>()

    val fragmentMainEventChannelFlow = fragmentMainEventChannel.receiveAsFlow()

    private lateinit var currentPath: String

    private var _currentRotation = 0

    val currentRotation get() = _currentRotation

    fun onPictureClicked() = launch(IO) {
        _currentRotation = if(_currentRotation == 0) 90 else 0
        fragmentMainEventChannel.send(FragmentMainEvent.RotateImageView(_currentRotation))
    }

    fun onPermissionResultReceived(granted: Boolean) = launch(IO) {
        if (granted) {
            takePicture()
        } else {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.permissionNotGrantedError))
        }
    }

    fun onCameraResultReceived(result: ActivityResult) = launch(IO) {
        val bitmap = BitmapFactory.decodeFile(currentPath)
        fragmentMainEventChannel.send(FragmentMainEvent.SetImageBitmapAsBackground(bitmap))
    }

    fun onFilePickerResultReceived(result: ActivityResult) = launch(IO) {
        result.data?.data?.let { uri ->
            val inputStream = app.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val rotatedBy = uriToExifDegrees(uri)
            fragmentMainEventChannel.send(FragmentMainEvent.SetImageBitmapAsBackground(bitmap, rotatedBy))
        }
    }

    private fun uriToExifDegrees(uri: Uri) : Int {
        val exif = if (Build.VERSION.SDK_INT > 23) {
            val stream = app.contentResolver.openInputStream(uri) ?: return 0
            ExifInterface(stream)
        } else {
            val path = uri.path ?: return 0
            ExifInterface(path)
        }

        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun takePicture() = launch(IO) {
        createTempImageFile()?.also { imageFile ->
            val imageURI = FileProvider.getUriForFile(app, AUTHORITY, imageFile)
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).let { intent ->
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                fragmentMainEventChannel.send(FragmentMainEvent.LaunchActivityResult(intent))
            }
        }
    }

    private fun createTempImageFile(): File? = try {
        val timeStamp: String = System.currentTimeMillis().toString()
        val storageDir: File? = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile("$JPG_PREFIX$timeStamp", JPG_SUFFIX, storageDir).also {
            currentPath = it.absolutePath
        }
    } catch (e: Exception) {
        null
    }


    sealed class FragmentMainEvent {
        class LaunchActivityResult(val intent: Intent) : FragmentMainEvent()
        class SetImageBitmapAsBackground(val bitmap: Bitmap, val rotatedBy: Int = 0) : FragmentMainEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : FragmentMainEvent()
        class RotateImageView(val rotatedBy: Int): FragmentMainEvent()
    }

    companion object {
        private const val JPG_PREFIX = "JPEG_"
        private const val JPG_SUFFIX = ".jpg"
        private const val AUTHORITY = "com.serverless.forschungsprojectfaas.fileprovider"
    }

}