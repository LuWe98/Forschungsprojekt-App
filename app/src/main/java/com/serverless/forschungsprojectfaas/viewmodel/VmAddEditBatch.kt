package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAddEditBatchArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VmAddEditBatch @Inject constructor(
    private val roomRepo: LocalRepository,
    private val navDispatcher: NavigationEventDispatcher,
    state: SavedStateHandle
) : ViewModel() {

    private val args = DfAddEditBatchArgs.fromSavedStateHandle(state)

    private val parsedBatch: Batch? = args.batch

    val dialogTitleRes get() = if(parsedBatch == null) R.string.addBatch else R.string.editBatch


    private var _caption: String = parsedBatch?.caption ?: ""

    val caption get() = _caption

    private val colorMutableStateFlow = MutableStateFlow(parsedBatch?.colorInt ?: Color.CYAN)

    val colorStateFlow = colorMutableStateFlow.asStateFlow()

    private val color get() = colorMutableStateFlow.value

    fun onCaptionTextChanged(newCaption: String) {
        _caption = newCaption
    }

    fun onCancelButtonClicked() {
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onColorBtnClicked(){
        launch {
            navDispatcher.dispatch(NavigationEvent.FromAddEditBatchToColorSelection(color))
        }
    }

    fun onConfirmButtonClicked() {
        launch {
            val batch = parsedBatch?.copy(caption = caption, colorInt = color) ?: Batch(caption = caption, colorInt = color)
            if(parsedBatch == null) {
                roomRepo.insert(batch)
            } else {
                roomRepo.update(batch)
            }
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onColorPickerResultReceived() {
        colorMutableStateFlow.value = Color.CYAN
    }

}