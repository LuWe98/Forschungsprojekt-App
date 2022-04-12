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
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.app
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.extensions.saveToInternalStorage
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import javax.inject.Inject


@HiltViewModel
class VmAdd @Inject constructor(
    application: Application,
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository
) : AndroidViewModel(application) {

    private val fragmentMainEventChannel = Channel<FragmentMainEvent>()

    val fragmentMainEventChannelFlow = fragmentMainEventChannel.receiveAsFlow()


    private val rotationMutableStateFlow = MutableStateFlow(0)

    val rotationStateFlow = rotationMutableStateFlow.asStateFlow()

    private val bitmapMutableStateFlow = MutableStateFlow<Bitmap?>(null)

    val bitmapStateFlow = bitmapMutableStateFlow.asStateFlow()

    private val currentBitmap get() = bitmapMutableStateFlow.value

    private var _title = ""

    val title get() = _title


    private lateinit var currentPath: String

    private var _currentRotation = 0

    val currentRotation get() = _currentRotation

    fun onPictureClicked() = launch(IO) {
        _currentRotation = if (_currentRotation == 360) 0 else _currentRotation + 90
        rotationMutableStateFlow.value = _currentRotation
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
        File(currentPath).apply {
            if (exists()) delete()
        }
        rotationMutableStateFlow.value = 0
        bitmapMutableStateFlow.value = bitmap
    }

    fun onFilePickerResultReceived(result: ActivityResult) = launch(IO) {
        result.data?.data?.let { uri ->
            val inputStream = app.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val rotatedBy = uriToExifDegrees(uri)
            rotationMutableStateFlow.value = rotatedBy
            bitmapMutableStateFlow.value = bitmap
        }
    }

    fun onBackButtonClicked() = launch(IO) {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    //TODO -> Hier dann das Senden an den Server für die Auswertung
    fun onSaveButtonClicked() = launch(IO) {
        if(currentBitmap == null) {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorNoBitmapSelected))
            return@launch
        }

        if(title.isBlank()) {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorTitleIsMissing))
            return@launch
        }


        navDispatcher.dispatch(NavigationEvent.NavigateToLoadingDialog(R.string.saving))

        runCatching {
            PictureEntry(
                name = _title,
                pictureUri = currentBitmap!!.saveToInternalStorage(app)
            )
        }.also {
            navDispatcher.dispatch(NavigationEvent.PopLoadingDialog)
        }.onSuccess {
            localRepository.insert(it)
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }.onFailure {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorCouldNotSafeFile))
        }
    }


    fun onTitleChanged(newTitle: String) {
        _title = newTitle
    }

    private fun uriToExifDegrees(uri: Uri): Int {
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
        app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let { storageDir ->
            File.createTempFile(System.currentTimeMillis().toString(), JPG_SUFFIX, storageDir).also { file ->
                currentPath = file.absolutePath
            }
        }
    } catch (e: Exception) {
        null
    }


    sealed class FragmentMainEvent {
        class LaunchActivityResult(val intent: Intent) : FragmentMainEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : FragmentMainEvent()
    }

    companion object {
        private const val JPG_SUFFIX = ".jpg"
        private const val AUTHORITY = "com.serverless.forschungsprojectfaas.fileprovider"
    }

}