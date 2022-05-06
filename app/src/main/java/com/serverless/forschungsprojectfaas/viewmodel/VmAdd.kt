package com.serverless.forschungsprojectfaas.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
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
import com.serverless.forschungsprojectfaas.model.EvaluationStatus
import com.serverless.forschungsprojectfaas.model.ktor.RemoteRepository
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.*
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
        if (currentBitmap == null) {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorNoBitmapSelected))
            return@launch
        }

        if (title.isBlank()) {
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorTitleIsMissing))
            return@launch
        }

        navDispatcher.dispatch(NavigationEvent.NavigateToLoadingDialog(R.string.evaluating))

        runCatching {
            val rotatedBitmap = currentBitmap!!.rotate(degree = rotationStateFlow.value)
            //bitmapMutableStateFlow.value = rotatedBitmap
            //base64Tests(rotatedBitmap)
            Pile(
                title = _title,
                pictureUri = rotatedBitmap.saveToInternalStorage(app),
                evaluationStatus = EvaluationStatus.NOT_EVALUATED
            )
        }.also {
            navDispatcher.dispatch(NavigationEvent.PopLoadingDialog)
        }.onSuccess { pile ->
            //TODO -> Die Insertions direkt machen. Und auch direkt zurücknavigieren. Evaluation Status anpassen und im Home Screen anzeigen
            localRepository.insert(pile)
            loadPileBatchesWithBars(pile).let {
                localRepository.insert(it.map(BatchWithBars::batch))
                localRepository.insert(it.flatMap(BatchWithBars::bars))
            }
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




    private fun loadPileBatchesWithBars(pile: Pile): List<BatchWithBars> {
        val stream = InputStreamReader(app.assets.open("results.csv"))
        val reader = BufferedReader(stream)
        val batchMap = HashMap<String, Pair<Batch, MutableList<Bar>>>()
        reader.lines().forEach { line ->
            val split = line.split(",")
            val caption = split[0]

            val rect = RectF(
                split[1].toFloat(),
                split[2].toFloat(),
                split[3].toFloat(),
                split[4].toFloat()
            )

            val batch: Pair<Batch, MutableList<Bar>> = batchMap.getOrElse(caption) {
                Pair(Batch(caption = caption), mutableListOf())
            }

            batch.second.add(
                Bar(
                    batchId = batch.first.batchId,
                    pileId = pile.pileId,
                    rect = rect
                )
            )
            batchMap[caption] = batch
        }

        return batchMap.map {
            BatchWithBars(
                batch = it.value.first,
                bars= it.value.second
            )
        }
    }

    private fun base64Tests(bitmap: Bitmap) = launch(scope = scope, dispatcher = IO) {
        log("START")
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val stringTest = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val file = File(app.filesDir, "my-file-name.txt")
        FileOutputStream(file).use { stream ->
            stream.write(byteArray)
        }
        log("FILE: $file")
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
    }

}