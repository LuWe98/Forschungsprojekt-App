package com.serverless.forschungsprojectfaas.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.model.PileStatus
import com.serverless.forschungsprojectfaas.model.ktor.ImageInformation
import com.serverless.forschungsprojectfaas.model.ktor.PotentialBox
import com.serverless.forschungsprojectfaas.model.ktor.RemoteRepository
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
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
    private val localRepository: LocalRepository,
    private val remoteRepo: RemoteRepository,
    private val applicationScope: CoroutineScope
) : AndroidViewModel(application) {

    companion object {
        private const val JPG_SUFFIX = ".jpg"
        private const val AUTHORITY = "com.serverless.forschungsprojectfaas.fileprovider"
    }

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

    fun onPictureClicked() = launch {
        rotationMutableStateFlow.value = if (rotation == 360) 90 else rotation + 90
    }

    fun onPermissionResultReceived(granted: Boolean) = launch {
        if (granted) {
            takePicture()
        } else {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.permissionNotGrantedError))
        }
    }

    fun onCameraResultReceived(result: ActivityResult) = launch {
        val bitmap = BitmapFactory.decodeFile(currentPath)
        File(currentPath).apply {
            if (exists()) delete()
        }
        rotationMutableStateFlow.value = 0
        bitmapMutableStateFlow.value = bitmap
    }

    fun onFilePickerResultReceived(result: ActivityResult) = launch(IO) {
        result.data?.data?.let { uri ->
            bitmapMutableStateFlow.value = uri.loadBitmap(app)
            rotationMutableStateFlow.value = uri.toExifDegrees(app)
        }
    }

    fun onBackButtonClicked() = launch {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    fun onAddButtonClicked() = launch {
        if (!validateInput()) return@launch

        val rotatedBitmap = currentBitmap!!.rotate(degree = rotationStateFlow.value)

        val pile = Pile(
            title = _title,
            pictureUri = rotatedBitmap.saveToInternalStorage(app),
            pileStatus = PileStatus.NOT_EVALUATED
        )

        localRepository.insert(pile)
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    //TODO -> Hier dann das Senden an den Server für die Auswertung
    fun onEvaluateButtonClicked() = launch(scope = applicationScope) {
        if (!validateInput()) return@launch

        val rotatedBitmap = currentBitmap!!.rotate(degree = rotationStateFlow.value)

        val pileUri = rotatedBitmap.saveToInternalStorage(app)

        val pile = Pile(
            title = _title,
            pictureUri = pileUri,
            pileStatus = PileStatus.EVALUATING
        )

        localRepository.insert(pile)
        navDispatcher.dispatch(NavigationEvent.NavigateBack)

        runCatching {
            val imageInformation = ImageInformation(
                name = pile.title,
                extension = pileUri.fileExtension ?: "png",
                image = rotatedBitmap.asBase64String(),
            )
            remoteRepo.uploadImageForProcessing(imageInformation)
        }.onFailure {
            localRepository.updatePileStatus(pile.pileId, PileStatus.FAILED)
        }.onSuccess { response ->
            val typeToken = object : TypeToken<List<PotentialBox>>() {}.type
            val boxes = Gson().fromJson<List<PotentialBox>>(response.bodyAsText(), typeToken)
            localRepository.insertBatchesAndBarsOfResponse(pile.pileId, boxes)
        }
    }

    private suspend fun validateInput(): Boolean {
        if (currentBitmap == null) {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorNoBitmapSelected))
            return false
        }
        if (title.isBlank()) {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorTitleIsMissing))
            return false
        }
        return true
    }

    fun onTitleChanged(newTitle: String) {
        _title = newTitle
    }

    private fun takePicture() = launch {
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
}