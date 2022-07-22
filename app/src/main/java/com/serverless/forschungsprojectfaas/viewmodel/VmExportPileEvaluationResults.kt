package com.serverless.forschungsprojectfaas.viewmodel

import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.ProjectApplication
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.extensions.appFilesDirectory
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.utils.Constants
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAlert.*
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfExportPileEvaluationResultsArgs
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class VmExportPileEvaluationResults @Inject constructor(
    state: SavedStateHandle,
    private val navDispatcher: NavigationEventDispatcher,
    private val app: ProjectApplication
) : ViewModel() {

    private val args = DfExportPileEvaluationResultsArgs.fromSavedStateHandle(state)

    private var _fileName = args.pileEvaluation?.pile?.title ?: ""

    val fileName get() = _fileName

    private val includeHeaderMutableStateFlow = MutableStateFlow(true)

    val includeHeaderStateFlow = includeHeaderMutableStateFlow.asStateFlow()

    private val includeHeader get() = includeHeaderMutableStateFlow.value


    fun onCancelButtonClicked() = launch {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    fun onIncludeHeaderLayoutClicked() {
        includeHeaderMutableStateFlow.value = !includeHeader
    }

    fun onFileNameTextChanged(text: String) {
        _fileName = text
    }

    fun onConfirmButtonClicked() {
        if (fileName.isBlank()) return

        exportFile?.let { file ->
            args.pileEvaluation?.rowEvaluationEntries?.let { entries ->
                val writer = OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)
                if (includeHeader) {
                    writer.write("Row,Caption,Count,From,To")
                    if (entries.isNotEmpty()) {
                        writer.write("\n")
                    }
                }
                entries.forEachIndexed { index, entry ->
                    writer.write(entry.asCsvLine)
                    if (index != entries.size - 1) {
                        writer.write("\n")
                    }
                }
                writer.close()
            }
        }
    }

    private val exportFile
        get(): File? {
            if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) return null
            return File(app.appFilesDirectory, "$fileName${Constants.CSV_FILE_SUFFIX}").let { subFolder ->
                if (subFolder.createNewFile()) {
                    subFolder
                } else {
                    launch {
                        navDispatcher.dispatch(NavigationEvent.NavigateToAlertDialog(AlertMessage.EXPORT_CSV_FILE_CREATION_ERROR))
                    }
                    null
                }
            }
        }
}