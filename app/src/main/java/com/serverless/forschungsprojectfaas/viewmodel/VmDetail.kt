package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.OwnApplication
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.div
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.junctions.BarWithBatch
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import com.serverless.forschungsprojectfaas.view.fragments.FragmentDetailArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class VmDetail @Inject constructor(
    private val app: OwnApplication,
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val args = FragmentDetailArgs.fromSavedStateHandle(state)

    private val pictureEntryStateFlow = localRepository
        .getPileWithBatchesFlow(args.pictureEntry.pileId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val pictureEntryNonNullStateFlow = pictureEntryStateFlow.mapNotNull { it }

    val imageBitmapStateFlow = pictureEntryNonNullStateFlow.map { entry ->
        BitmapFactory.decodeFile(entry.pile.pictureUri.path)
    }.flowOn(IO).stateIn(viewModelScope, SharingStarted.Eagerly, null).mapNotNull { it }

    val entryTitleFlow = pictureEntryNonNullStateFlow.map(PileWithBatches::pile / Pile::title).distinctUntilChanged()

    val barBatchWithBarsStateFlow = pictureEntryNonNullStateFlow.map {
        it.batches
    }

    var allBarsFlow = pictureEntryNonNullStateFlow.map {
        it.barsWithBatches.map(BarWithBatch::bar)
    }.distinctUntilChanged()


    fun onBackButtonClicked() = launch(IO) {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    fun onGoToBatchSelectionClicked() {
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateToBatchSelection())
        }
    }
}