package com.serverless.forschungsprojectfaas.viewmodel

import android.R.attr.bitmap
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.result.ActivityResult
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.model.ktor.RemoteRepository
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject


@HiltViewModel
class VmAdd @Inject constructor(
    application: Application,
    private val scope: CoroutineScope,
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository,
    private val remoteRepo: RemoteRepository
) : AndroidViewModel(application) {

    private val fragmentMainEventChannel = Channel<FragmentMainEvent>()

    val fragmentMainEventChannelFlow = fragmentMainEventChannel.receiveAsFlow()


    private val rotationMutableStateFlow = MutableStateFlow(0)

    val rotationStateFlow = rotationMutableStateFlow.asStateFlow()

    private val rotation get() = rotationMutableStateFlow.value

    private val bitmapMutableStateFlow = MutableStateFlow<Bitmap?>(null)

    val bitmapStateFlow = bitmapMutableStateFlow.asStateFlow()

    private val currentBitmap get() = bitmapMutableStateFlow.value

    private var _title = ""

    val title get() = _title

    private lateinit var currentPath: String


//    private var _currentRotation = 0
//
//    val currentRotation get() = _currentRotation

    fun onPictureClicked() = launch(IO) {
        rotationMutableStateFlow.value = if (rotation == 360) 90 else rotation + 90
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
            rotationMutableStateFlow.value = uriToExifDegrees(uri)
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

        navDispatcher.dispatch(NavigationEvent.NavigateToLoadingDialog(R.string.evaluating))

        runCatching {
            val rotatedBitmap = currentBitmap!!.rotate(degree = rotationStateFlow.value)
            //bitmapMutableStateFlow.value = rotatedBitmap

            PictureEntry(
                title = _title,
                pictureUri = rotatedBitmap.saveToInternalStorage(app)
            ).also {

//                Das ist der Code um Bitmap zu der Clound function zu senden
//                launch(scope = scope) {
//                    log("START")
//                    val byteArrayOutputStream = ByteArrayOutputStream()
//                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
//                    val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
//                    val stringTest = Base64.encodeToString(byteArray, Base64.DEFAULT)
//                    val response = remoteRepo.invokeTestFunction(stringTest)
//
//                    val responseString = response.bodyAsText()
//                    val base64StringOnly = responseString
//                        .substringAfter("\"")
//                        .substringBefore("\"")
//                        .replace("\\n", "")
//                        .replace("\\r", "")
//                    val decoded = Base64.decode(base64StringOnly, Base64.DEFAULT)
//                    val returnedBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
//                    bitmapMutableStateFlow.value = returnedBitmap
//                }
            }
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