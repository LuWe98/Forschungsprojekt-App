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
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAlert
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


    private var _firstBatchLetter: String = parsedBatch?.caption?.substring(0, 1) ?: ""

    val firstBatchLetter get() = _firstBatchLetter

    private var _secondBatchLetter: String = parsedBatch?.caption?.substring(1) ?: ""

    val secondBatchLetter get() = _secondBatchLetter

    private val colorMutableStateFlow = MutableStateFlow(parsedBatch?.colorInt ?: Color.CYAN)

    val colorStateFlow = colorMutableStateFlow.asStateFlow()

    private val color get() = colorMutableStateFlow.value

    fun onFirstBatchLetterChanged(text: String) {
        _firstBatchLetter = text
    }

    fun onSecondBatchLetterChanged(text: String) {
        _secondBatchLetter = text
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
        if (firstBatchLetter.isBlank() || secondBatchLetter.isBlank()) {
            //eventChannel.send(AddEditBatchEvent.ShowMessageSnackBar(R.string.errorCaptionCannotBeEmpty))
            navDispatcher.dispatch(NavigationEvent.NavigateToAlertDialog(DfAlert.AlertMessage.ADD_EDIT_BATCH_CAPTION_IS_EMPTY))
            return@launch
        }
        val caption = firstBatchLetter + secondBatchLetter
        roomRepo.findBatchWithCaption(caption)?.let {
            if(it.batchId == parsedBatch?.batchId) {
                return@let
            }
            //eventChannel.send(AddEditBatchEvent.ShowMessageSnackBar(R.string.errorCaptionAlreadyUsed))
            navDispatcher.dispatch(NavigationEvent.NavigateToAlertDialog(DfAlert.AlertMessage.ADD_EDIT_BATCH_CAPTION_ALREADY_USED))
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


    fun convertToValidText(text: String, textBefore: String): String = when {
        text.isEmpty() -> text
        text.length > 1 -> when (textBefore.length) {
            1 -> {
                when (text.indexOf(textBefore)) {
                    -1 -> text[0].toString()
                    0 -> text[1].toString()
                    else -> text[0].toString()
                }
            }
            else -> text[0].toString()
        }
        !text.matches("^[a-zA-Z]".toRegex()) -> ""
        text[0].isLowerCase() -> text.uppercase()
        else -> text
    }

    sealed class AddEditBatchEvent {
        data class ShowMessageSnackBar(@StringRes val res: Int) : AddEditBatchEvent()
    }

}