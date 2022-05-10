package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.FragmentResult
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class VmBatchSelection @Inject constructor(
    private val roomRepo: LocalRepository,
    private val navDispatcher: NavigationEventDispatcher,
    private val fragmentResultDispatcher: FragmentResultDispatcher
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
        launch {
            fragmentResultDispatcher.dispatch(FragmentResult.BatchSelectionResult(batch.batchId))
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onBatchLongClicked(batch: Batch) {
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateToAddEditBatch(batch))
        }
    }

    fun onAddBatchButtonClicked(){
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateToAddEditBatch())
        }
    }

}