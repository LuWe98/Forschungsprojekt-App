package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.OwnApplication
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.FragmentResult
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.div
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.model.BoxDimension
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import com.serverless.forschungsprojectfaas.view.custom.BarBatchDisplay
import com.serverless.forschungsprojectfaas.view.fragments.FragmentDetailArgs
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class VmDetail @Inject constructor(
    private val app: OwnApplication,
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val args = FragmentDetailArgs.fromSavedStateHandle(state)

    private val pileStateFlow: StateFlow<PileWithBatches?> = localRepository
        .getPileWithBatchesFlow(args.pile.pileId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val nonNullPileFlow = pileStateFlow.mapNotNull { it }

    private val imagePathFlow = nonNullPileFlow.map { it.pile.pictureUri.path }.distinctUntilChanged()

    val imageBitmapStateFlow = imagePathFlow.map(BitmapFactory::decodeFile).flowOn(IO).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pileTitleFlow = nonNullPileFlow.map(PileWithBatches::pile / Pile::title).distinctUntilChanged()

    val barBatchWithBarsStateFlow = nonNullPileFlow.map(PileWithBatches::batches::get)

    var allBarsFlow = nonNullPileFlow.map(PileWithBatches::bars::get).distinctUntilChanged()

    private val selectedBarIdsMutableStateFlow: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    val selectedBarIdsStateFlow: StateFlow<Set<String>> = selectedBarIdsMutableStateFlow.asStateFlow()

    private val selectedBarIds get() = selectedBarIdsStateFlow.value

    val selectedBarsStateFlow: StateFlow<List<Bar>> = combine(
        flow = selectedBarIdsMutableStateFlow,
        flow2 = allBarsFlow
    ) { ids, bars ->
        bars.filter { ids.contains(it.barId) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val selectedBars get() = selectedBarsStateFlow.value

    var barOpacity: Int = BarBatchDisplay.DEFAULT_BOX_ALPHA
        private set

    var barStroke: Int = BarBatchDisplay.DEFAULT_BOX_STROKE
        private set

    private val batchSearchQueryMutableStateFlow = MutableStateFlow("")

    val batchSearchQuery get() = batchSearchQueryMutableStateFlow.value

    val filteredBatchWithBarsFlow: Flow<List<BatchWithBars>> = combine(
        flow = batchSearchQueryMutableStateFlow,
        flow2 = barBatchWithBarsStateFlow
    ) { query, barsWithBatched ->
        barsWithBatched.filter {
            it.batch?.caption?.contains(query, true) == true
        }.sortedBy {
            it.batch?.caption
        }
    }


    fun isBarSelected(bar: Bar) = selectedBarIds.contains(bar.barId)


    fun onImageClicked(bar: Bar?, point: PointF) {
        bar?.let {
            log("ON BAR CLICKED $bar")
            selectedBarIdsMutableStateFlow.value = selectedBarIds.toMutableSet().apply {
                if (isBarSelected(bar)) {
                    remove(bar.barId)
                } else {
                    add(bar.barId)
                }
            }
        } ?: run {
            log("NOT ON BAR CLICKED $point")
        }
    }

    fun onImageLongClicked(bar: Bar?, point: PointF) {
        bar?.let {
            log("ON BAR LONG CLICKED $bar")
        } ?: run {
            log("NOT ON BAR LONG CLICKED $point")
            addNewBarToPile(point)
        }
    }

    fun onBarDragReleased(bar: Bar) {
        launch {
            localRepository.update(bar)
        }
    }

    private fun addNewBarToPile(point: PointF) = launch {
        pileStateFlow.value?.let { pile ->
            val rectToInsert = RectF(
                max(point.x - pile.averageBoxSize.width / 2, 0f),
                max(point.y - pile.averageBoxSize.height / 2, 0f),
                point.x + pile.averageBoxSize.width / 2,
                point.y + pile.averageBoxSize.height / 2
            )
            val barToInsert = Bar(
                pileId = pile.pile.pileId,
                rect = rectToInsert
            )
            localRepository.insert(barToInsert)
        }
    }


    /**
     * Die Auswahl aufheben
     */
    fun onClearSelectionClicked() {
        selectedBarIdsMutableStateFlow.value = emptySet()
    }

    /**
     * Dafür da, das viele bars in einer Line selektiert werden können, dadurch kann man diese leichter austauschen
     */
    fun onSelectBarsInLineClicked() = launch {
        if (selectedBarIds.size != 2) return@launch
        selectedBars.let {
            val left: Bar
            val right: Bar

            if (it[0].rect.left < it[1].rect.left) {
                left = it[0]
                right = it[1]
            } else {
                left = it[1]
                right = it[0]
            }

            val leftOfRightBars = pileStateFlow.value!!.findNearestLeftBars(right)
            val rightOfLeftBars = pileStateFlow.value!!.findNearestRightBars(left)
            val ids = leftOfRightBars.filter { bar ->
                rightOfLeftBars.contains(bar)
            }

            selectedBarIdsMutableStateFlow.value = selectedBarIds.toMutableSet().apply {
                addAll(ids.map { bar ->
                    bar.barId
                })
            }
        }
    }

    /**
     * Die Batch zugehörigkeit der Auswahl ändern
     */
    fun onSwapBatchOfSelectedBarsClicked() = launch {
        if (selectedBarIds.isEmpty()) return@launch
        navDispatcher.dispatch(NavigationEvent.NavigateToBatchSelection())
    }

    fun onBatchSelectionResultReceived(result: FragmentResult.BatchSelectionResult) = launch {
        selectedBars.let { bars ->
            localRepository.update(bars.map { bar ->
                bar.copy(batchId = result.batchId)
            })
        }
        onClearSelectionClicked()
    }

    /**
     * Die Boxen der Auswahl löschen
     */
    fun onDeleteSelectedBarsClicked() = launch {
        if (selectedBarIds.isEmpty()) return@launch
        localRepository.delete(selectedBars)
        onClearSelectionClicked()
    }


    fun onBackButtonClicked() = launch {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    fun onOpacityProgressChanged(progress: Int) {
        barOpacity = progress
    }

    fun onStrokeWidthProgressChanged(progress: Int) {
        barStroke = progress
    }

    fun onBatchSearchQueryChanged(newQuery: String) {
        batchSearchQueryMutableStateFlow.value = newQuery
    }

    fun onWidthProgressChanged(progress: Int, isUserInput: Boolean) {
        if (!isUserInput) return

        launch {
            selectedBars.first().let { bar ->
                bar.rect.set(
                    bar.rect.centerX() - progress / 2f,
                    bar.rect.top,
                    bar.rect.centerX() + progress / 2f,
                    bar.rect.bottom
                )
                localRepository.update(bar)
            }
        }
    }

    fun onHeightProgressChanged(progress: Int, isUserInput: Boolean) {
        if (!isUserInput) return

        launch {
            selectedBars.first().let { bar ->
                bar.rect.set(
                    bar.rect.left,
                    bar.rect.centerY() - progress / 2f,
                    bar.rect.right,
                    bar.rect.centerY() + progress / 2f
                )
                localRepository.update(bar)
            }
        }
    }


//
//    private fun onLongClickedTest(bar: Bar) {
//        pileStateFlow.value!!.findBarsInLine(bar).let { bars ->
//            selectedBarIdsMutableStateFlow.value = selectedBarIds.toMutableSet().apply {
//                val ids = bars.map { it.barId }
//                if (selectedBarIds.any { ids.contains(it) }) {
//                    removeAll(ids)
//                } else {
//                    addAll(ids)
//                }
//            }
//        }
//    }
}