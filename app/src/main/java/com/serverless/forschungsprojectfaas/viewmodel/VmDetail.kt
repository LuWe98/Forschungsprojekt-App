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
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.CapturedPicture
import com.serverless.forschungsprojectfaas.model.room.entities.BarBatch
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.junctions.BarBatchWithBars
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
        .getPictureEntryFlowWithId(args.pictureEntry.id)
        .stateIn(viewModelScope, SharingStarted.Lazily, args.pictureEntry)

    private val imageBitmapStateFlow = pictureEntryStateFlow.map { entry ->
        BitmapFactory.decodeFile(entry.pictureUri.path)
    }.flowOn(IO).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val imageBitmapFlow = imageBitmapStateFlow.mapNotNull { it }

    val entryTitleFlow = pictureEntryStateFlow.map(CapturedPicture::title::get).distinctUntilChanged()

    private val barBatchWithBarsMutableStateFlow = MutableStateFlow(emptyList<BarBatchWithBars>())

    val barBatchWithBarsStateFlow = barBatchWithBarsMutableStateFlow.asStateFlow()

    var allBarsFlow = barBatchWithBarsMutableStateFlow.map {
        it.flatMap {
            it.bars
        }
    }.distinctUntilChanged()


    fun onBackButtonClicked() = launch(IO) {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    init {
        loadResultRectangles()
    }

    private fun loadResultRectangles() {
        val stream = InputStreamReader(app.assets.open("results.csv"))
        val reader = BufferedReader(stream)
        val entries = mutableListOf<Bar>()
        val batchMap = HashMap<String, Pair<BarBatch, MutableList<Bar>>>()
        reader.lines().forEach { line ->
            val split = line.split(",")
            val rect = RectF(
                split[1].toFloat(),
                split[2].toFloat(),
                split[3].toFloat(),
                split[4].toFloat()
            )
            val batch: Pair<BarBatch, MutableList<Bar>> = batchMap.getOrDefault(split[0], Pair(BarBatch(caption = split[0], pictureId = ""), mutableListOf()))
            batch.second.add(Bar(
                batchId = batch.first.id,
                rect = rect
            ))
            batchMap[split[0]] = batch
        }
        val batchesWithBars = batchMap.map {
            val batch = it.value.first
            val bars = it.value.second
            BarBatchWithBars(batch, bars)
        }

        barBatchWithBarsMutableStateFlow.value = batchesWithBars
    }
}