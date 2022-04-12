package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.*
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent.*
import com.serverless.forschungsprojectfaas.dispatcher.selection.OrderByItem
import com.serverless.forschungsprojectfaas.dispatcher.selection.PictureMoreOptions
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
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
        localRepository.getAllPictureEntries(query)
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onFabClicked() = launch(IO) {
        navDispatcher.dispatch(NavigateToAddScreen)
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQueryMutableStatFlow.value = newQuery
    }

    fun onRvaItemClicked(entry: PictureEntry) = launch(IO) {
        navDispatcher.dispatch(NavigateToDetailScreen(entry))
    }

    fun onRvaItemMoreOptionsClicked(entry: PictureEntry) = launch(IO) {
        navDispatcher.dispatch(NavigateToSelectionDialog(SelectionRequestType.PictureMoreOptionsSelection(entry)))
    }

    fun onPictureMoreOptionsResultReceived(result: SelectionResult.PictureMoreOptionsSelectionResult) {
        when(result.selectedItem) {
            PictureMoreOptions.DELETE -> onDeletePictureEntrySelected(result.calledOnPictureEntry)
            PictureMoreOptions.OPEN -> onRvaItemClicked(result.calledOnPictureEntry)
        }
    }

    fun onSortButtonClicked() = launch(IO) {
        navDispatcher.dispatch(NavigateToSelectionDialog(SelectionRequestType.OrderBySelection(orderByMutableStateFlow.value)))
    }

    private fun onDeletePictureEntrySelected(entry: PictureEntry) = launch(IO) {
        entry.pictureUri.path?.let { path ->
            File(path).let { file ->
                if(file.exists()) {
                    file.delete()
                }
            }
        }
        localRepository.delete(entry)
    }

    fun onOrderBySelectionResultReceived(result: SelectionResult.OrderBySelectionResult) = launch(IO) {
        orderByMutableStateFlow.value = result.selectedItem
    }
    
}