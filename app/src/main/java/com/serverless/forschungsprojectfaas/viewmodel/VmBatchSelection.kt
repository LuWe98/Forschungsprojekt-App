package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class VmBatchSelection @Inject constructor(
    private val roomRepo: LocalRepository,
    private val navDispatcher: NavigationEventDispatcher
): ViewModel() {

    private val searchQueryMutableStateFlow = MutableStateFlow("")

    val filteredBatchesFlow: Flow<List<Batch>> = searchQueryMutableStateFlow.flatMapLatest { query ->
        roomRepo.getFilteredBatchesFlow(query)
    }

    fun onSearchQueryChanged(query: String) {
        searchQueryMutableStateFlow.value = query
    }

    fun onBackButtonClicked() {
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onBatchClicked(batch: Batch) {

    }

    fun onBatchLongClicked(batch: Batch) {
        launch {
            navDispatcher.dispatch(NavigationEvent.FromBatchSelectionToAddEditBatch(batch))
        }
    }

    fun onAddBatchButtonClicked(){
        launch {
            navDispatcher.dispatch(NavigationEvent.FromBatchSelectionToAddEditBatch())
        }
    }

}