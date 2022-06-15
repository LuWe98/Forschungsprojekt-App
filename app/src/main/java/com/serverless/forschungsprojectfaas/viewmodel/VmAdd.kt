package com.serverless.forschungsprojectfaas.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.serverless.forschungsprojectfaas.model.PileStatus
import com.serverless.forschungsprojectfaas.model.ktor.ProcessedBox
import com.serverless.forschungsprojectfaas.model.ktor.ProcessedPilesResponse
import com.serverless.forschungsprojectfaas.model.ktor.RemoteRepository
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
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

    //TODO -> Hier dann das Senden an den Server für die Auswertung
    fun onSaveButtonClicked() = launch {
        if (!validateInput()) return@launch

        navDispatcher.dispatch(NavigationEvent.NavigateToLoadingDialog(R.string.evaluating))

        runCatching {
            val rotatedBitmap = currentBitmap!!.rotate(degree = rotationStateFlow.value)
            //bitmapMutableStateFlow.value = rotatedBitmap
            //base64Tests(rotatedBitmap)
            Pile(
                title = _title,
                pictureUri = rotatedBitmap.saveToInternalStorage(app),
                pileStatus = PileStatus.NOT_EVALUATED
            )
            //TODO -> Im Application Scope eine coroutine Aufrufen um ein Ergebnis zu erhalten.
        }.onSuccess { pile ->
            //TODO -> Die Insertions direkt machen. Und auch direkt zurücknavigieren. Evaluation Status anpassen und im Home Screen anzeigen
            localRepository.insert(pile)
            //Das ist wird später durch dynamische
            insertBatchesAndBarsOfResponse(pile)

            navDispatcher.dispatch(NavigationEvent.PopLoadingDialog)
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }.onFailure {
            navDispatcher.dispatch(NavigationEvent.PopLoadingDialog)
            fragmentMainEventChannel.send(FragmentMainEvent.ShowMessageSnackBar(R.string.errorCouldNotSafeFile))
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


    private suspend fun insertBatchesAndBarsOfResponse(pile: Pile) = withContext(IO) {
        // Das ist die emulierte Response! -> Das wird von der Function zurückgeliefert
        val response = generateFunctionResponse()

        val groupedByCaption = response.processedBoxes.groupBy(ProcessedBox::caption)
        val localBatches = localRepository.findBatchesWithCaptions(groupedByCaption.keys)

        val batchesToInsert = mutableListOf<Batch>()
        val barsToInsert = mutableListOf<Bar>()

        groupedByCaption.forEach { (caption, boxes) ->
            // Es wird gecheckt, ob schon ein Batch mit der Caption in der Datenbank vorhanden ist
            // Plausibilitätsprüfungen hier durchführen -> Oder auch schon vor dem Group by !
            val batchId: String? = if (caption.length != 2) {
                null
            } else {
                localBatches.firstOrNull { it.caption == caption } ?: Batch(caption = caption).also(batchesToInsert::add)
            }?.batchId

            boxes.map { box ->
                Bar(
                    batchId = batchId,
                    pileId = pile.pileId,
                    rect = box.rect
                )
            }.also(barsToInsert::addAll)
        }

        //Inserted die Daten in einer Transaktion -> Nur alle Insert Operationen gelingen oder keine.
        localRepository.insertBatchesAndBars(
            batches = batchesToInsert,
            bars = runValidityChecks(barsToInsert, localBatches + batchesToInsert)
        )
    }


    //TODO -> Die checks noch einbauen
    //Checks the boxes of the Response for validity
    // explanation:
    // -> Captions With Single Letter
    // -> Captions wich are lonely unterwegs -> Find the ones that are lonely and find most ones in a row
    private fun runValidityChecks(bars: List<Bar>, batches: List<Batch>): List<Bar> {
        val averageBarDimensions = bars.averageBarDimensions
        val batchMap = batches.associateBy(Batch::batchId)

        return bars.filterOverlappingBars(0.85f)
            .fixBarDimensions(averageBarDimensions)
            .filterIsolatedBars(averageBarDimensions)
            .adjustBatchIdsIfPossible(2, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(5)
            .adjustSpacesBetweenBatchGroups(4)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
            .adjustLonelyBarsBetween(3, 1f, batchMap)
            .adjustBatchIdsIfPossible(1, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
            .adjustLonelyBarsBetween(3, 0.75f, batchMap)
            .adjustBatchIdsIfPossible(1, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
            .adjustLonelyBarsBetween(3, 0.5f, batchMap)
            .adjustBatchIdsIfPossible(1, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
    }



    //Die Methode ist nur dafür da, um eine beispielhafte Response eines Function Aufrufes zu simulieren
    private fun generateFunctionResponse(): ProcessedPilesResponse {
        val stream = InputStreamReader(app.assets.open("results.csv"))
        val processedBoxes: List<ProcessedBox> = BufferedReader(stream).lineSequence().map { line ->
            line.split(",").let { columns ->
                ProcessedBox(
                    caption = columns[0],
                    left = columns[1].toFloat(),
                    top = columns[2].toFloat(),
                    right = columns[3].toFloat(),
                    bottom = columns[4].toFloat()
                )
            }
        }.toList()

        // Das ist die emulierte Response! -> Das wird von der Function zurückgeliefert
        return ProcessedPilesResponse(processedBoxes)
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