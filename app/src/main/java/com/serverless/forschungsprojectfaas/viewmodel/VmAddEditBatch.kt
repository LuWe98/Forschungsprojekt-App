package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.Color
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAddEditBatchArgs
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmAddEditBatch @Inject constructor(
    private val roomRepo: LocalRepository,
    private val navDispatcher: NavigationEventDispatcher,
    state: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<AddEditBatchEvent>()

    val eventChannelFlow = eventChannel.receiveAsFlow()


    private val args = DfAddEditBatchArgs.fromSavedStateHandle(state)

    private val parsedBatch: Batch? = args.batch

    val isAddMode get() = parsedBatch == null

    val dialogTitleRes get() = if (isAddMode) R.string.addBatch else R.string.editBatch


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

    fun onColorBtnClicked() {
        launch {
            navDispatcher.dispatch(NavigationEvent.FromAddEditBatchToColorSelection(color))
        }
    }

    fun onColorSelectionResultReceived(result: FragmentResultDispatcher.FragmentResult.ColorPickerResult) {
        colorMutableStateFlow.value = result.selectedColor
    }

    fun onDeleteBatchButtonClicked() {
        launch {
            roomRepo.delete(parsedBatch!!)
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onConfirmButtonClicked() = launch {
        if (caption.isBlank()) {
            eventChannel.send(AddEditBatchEvent.ShowMessageSnackBar(R.string.errorCaptionCannotBeEmpty))
            return@launch
        }

        roomRepo.findBatchWithCaption(caption)?.let {
            if(it.batchId == parsedBatch?.batchId) {
                return@let
            }

            eventChannel.send(AddEditBatchEvent.ShowMessageSnackBar(R.string.errorCaptionAlreadyUsed))
            return@launch
        }

        val batch = parsedBatch?.copy(caption = caption, colorInt = color) ?: Batch(caption = caption, colorInt = color)
        if (isAddMode) {
            roomRepo.insert(batch)
        } else {
            roomRepo.update(batch)
        }
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    sealed class AddEditBatchEvent {
        data class ShowMessageSnackBar(@StringRes val res: Int) : AddEditBatchEvent()
    }

}