package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.SelectionResult
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent.*
import com.serverless.forschungsprojectfaas.dispatcher.selection.OrderByItem
import com.serverless.forschungsprojectfaas.dispatcher.selection.PictureMoreOptions
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.model.PileStatus
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository
): ViewModel() {

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
        launch {
            navDispatcher.dispatch(NavigateToDetailScreen(pile))
        }
    }

    fun onRvaItemMoreOptionsClicked(pile: Pile) {
        launch {
            navDispatcher.dispatch(NavigateToSelectionDialog(SelectionRequestType.PictureMoreOptionsSelection(pile)))
        }
    }

    //TODO -> Aktionen dann durchführen hier
    fun onRvaStatusButtonClicked(pile: Pile) {
        launch {
            when(pile.pileStatus) {
                PileStatus.FAILED -> {

                }
                PileStatus.NOT_EVALUATED -> {

                }
                PileStatus.EVALUATING -> {

                }
                PileStatus.LOCALLY_CHANGED -> {

                }
                PileStatus.UPLOADED -> {

                }
            }
        }
    }

    fun onPileMoreOptionsResultReceived(result: SelectionResult.PictureMoreOptionsSelectionResult) {
        when(result.selectedItem) {
            PictureMoreOptions.DELETE -> onDeletePileEntrySelected(result.calledOnPile)
            PictureMoreOptions.OPEN -> onRvaItemClicked(result.calledOnPile)
            PictureMoreOptions.EXPORT -> onExportToCsvClicked(result.calledOnPile)
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
}