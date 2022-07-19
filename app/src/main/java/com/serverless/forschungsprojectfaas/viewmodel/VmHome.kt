package com.serverless.forschungsprojectfaas.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serverless.forschungsprojectfaas.ProjectApplication
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.SelectionResult
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent.*
import com.serverless.forschungsprojectfaas.dispatcher.selection.OrderByItem
import com.serverless.forschungsprojectfaas.dispatcher.selection.PictureMoreOptions
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.model.PileStatus.*
import com.serverless.forschungsprojectfaas.model.ktor.PotentialBox
import com.serverless.forschungsprojectfaas.model.ktor.RemoteRepository
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val applicationScope: CoroutineScope,
    private val app: ProjectApplication
): ViewModel() {

    private val fragmentHomeEventChannel = Channel<FragmentHomeEvent>()

    val fragmentHomeEventChannelFlow = fragmentHomeEventChannel.receiveAsFlow()

    init {
        launch {
            localRepository.resetCurrentlyEvaluatingPiles()
        }
    }

    private val searchQueryMutableStatFlow = MutableStateFlow("")

    val searchQuery get() = searchQueryMutableStatFlow.value

    private val orderByMutableStateFlow = MutableStateFlow(OrderByItem.TITLE)

    val pilesWithBarCount: StateFlow<List<PileWithBarCount>> = combine(
        searchQueryMutableStatFlow,
        orderByMutableStateFlow
    ) { query, orderBy ->
        localRepository.getPilesWithBarCount(query)
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onFabClicked() = launch {
        navDispatcher.dispatch(NavigateToAddScreen)
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQueryMutableStatFlow.value = newQuery
    }

    fun onRvaItemClicked(pile: Pile) {
        if(pile.pileStatus == NOT_EVALUATED) {
            onRvaItemMoreOptionsClicked(pile)
            return
        }

        launch {
            navDispatcher.dispatch(NavigateToDetailScreen(pile))
        }
    }

    fun onRvaItemMoreOptionsClicked(pile: Pile) {
        launch {
            navDispatcher.dispatch(
                NavigateToSelectionDialog(
                    SelectionRequestType.PictureMoreOptionsSelection(
                        pile,
                        pile.moreOptionsSelectionOptions
                    )
                )
            )
        }
    }

    //TODO -> Aktionen dann durchführen hier
    fun onRvaStatusButtonClicked(pile: Pile) {
        launch {
            when(pile.pileStatus) {
                FAILED -> {}
                NOT_EVALUATED -> {}
                EVALUATING -> {}
                LOCALLY_CHANGED -> {}
                UPLOADED -> {}
            }
        }
    }

    fun onPileMoreOptionsResultReceived(result: SelectionResult.PictureMoreOptionsSelectionResult) {
        when(result.selectedItem) {
            PictureMoreOptions.DELETE -> onDeletePileEntrySelected(result.calledOnPile)
            PictureMoreOptions.OPEN -> onRvaItemClicked(result.calledOnPile)
            PictureMoreOptions.EXPORT -> onExportToCsvClicked(result.calledOnPile)
            PictureMoreOptions.UPLOAD_CHANGES -> onUploadChangesSelected(result.calledOnPile)
            PictureMoreOptions.EVALUATE -> onEvaluatePileSelected(result.calledOnPile)
        }
    }

    fun onSortButtonClicked() = launch {
        navDispatcher.dispatch(NavigateToSelectionDialog(SelectionRequestType.OrderBySelection(orderByMutableStateFlow.value)))
    }

    private fun onDeletePileEntrySelected(captured: Pile) = launch {
        captured.pictureUri.path?.let { path ->
            File(path).let { file ->
                if(file.exists()) {
                    file.delete()
                }
            }
        }
        localRepository.delete(captured)
    }

    fun onOrderBySelectionResultReceived(result: SelectionResult.OrderBySelectionResult) = launch {
        orderByMutableStateFlow.value = result.selectedItem
    }

    private fun onExportToCsvClicked(pile: Pile) = launch {
        localRepository.getPileWithBatches(pile.pileId).let {
            navDispatcher.dispatch(NavigateToExportPileEvaluationResult(it.asPileEvaluation))
        }
    }

    private fun onEvaluatePileSelected(pile: Pile) = launch(scope = applicationScope) {
        runCatching {
            localRepository.updatePileStatus(pile.pileId, EVALUATING)
            remoteRepository.uploadImageForProcessing(pile.asImageInformationRequest())
        }.onFailure {
            localRepository.updatePileStatus(pile.pileId, FAILED)
        }.onSuccess { response ->
            val typeToken = object : TypeToken<List<PotentialBox>>() {}.type
            val boxes = Gson().fromJson<List<PotentialBox>>(response.bodyAsText(), typeToken)
            localRepository.insertBatchesAndBarsOfResponse(pile.pileId, boxes)
        }
    }

    private fun onUploadChangesSelected(pile: Pile) = launch(scope = applicationScope) {
        val pileWithBatches = localRepository.getPileWithBatches(pile.pileId)

        navDispatcher.dispatch(NavigateToLoadingDialog(R.string.uploadingChanges))

        runCatching {
            remoteRepository.persistUpdatedResults(pileWithBatches.asImageInformation)
        }.also {
            delay(500)
            navDispatcher.dispatch(PopLoadingDialog)
        }.onFailure {
            fragmentHomeEventChannel.send(FragmentHomeEvent.DisplaySnackBar(R.string.errorCouldNotUploadUpdate))
        }.onSuccess {
            localRepository.updatePileStatus(pile.pileId, UPLOADED)
            fragmentHomeEventChannel.send(FragmentHomeEvent.DisplaySnackBar(R.string.successChangesWereUploaded))
        }
    }


    sealed class FragmentHomeEvent {
        class DisplaySnackBar(@StringRes val messageRes: Int): FragmentHomeEvent()
    }

}