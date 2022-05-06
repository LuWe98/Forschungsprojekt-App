package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.SelectionResult
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent.*
import com.serverless.forschungsprojectfaas.dispatcher.selection.OrderByItem
import com.serverless.forschungsprojectfaas.dispatcher.selection.PictureMoreOptions
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
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

    val pictureEntryFlow = combine(
        searchQueryMutableStatFlow,
        orderByMutableStateFlow
    ) { query, orderBy ->
        localRepository.getAllPilesFlow(query)
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onFabClicked() = launch(IO) {
        navDispatcher.dispatch(NavigateToAddScreen)
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQueryMutableStatFlow.value = newQuery
    }

    fun onRvaItemClicked(captured: Pile) = launch(IO) {
        navDispatcher.dispatch(NavigateToDetailScreen(captured))
    }

    fun onRvaItemMoreOptionsClicked(captured: Pile) = launch(IO) {
        navDispatcher.dispatch(NavigateToSelectionDialog(SelectionRequestType.PictureMoreOptionsSelection(captured)))
    }

    fun onPictureMoreOptionsResultReceived(result: SelectionResult.PictureMoreOptionsSelectionResult) {
        when(result.selectedItem) {
            PictureMoreOptions.DELETE -> onDeletePictureEntrySelected(result.calledOnPile)
            PictureMoreOptions.OPEN -> onRvaItemClicked(result.calledOnPile)
        }
    }

    fun onSortButtonClicked() = launch(IO) {
        navDispatcher.dispatch(NavigateToSelectionDialog(SelectionRequestType.OrderBySelection(orderByMutableStateFlow.value)))
    }

    private fun onDeletePictureEntrySelected(captured: Pile) = launch(IO) {
        captured.pictureUri.path?.let { path ->
            File(path).let { file ->
                if(file.exists()) {
                    file.delete()
                }
            }
        }
        localRepository.delete(captured)
    }

    fun onOrderBySelectionResultReceived(result: SelectionResult.OrderBySelectionResult) = launch(IO) {
        orderByMutableStateFlow.value = result.selectedItem
    }
    
}