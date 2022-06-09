package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.FragmentResult
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
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

@HiltViewModel
class VmDetail @Inject constructor(
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val args = FragmentDetailArgs.fromSavedStateHandle(state)

    private val pileStateFlow: StateFlow<PileWithBatches?> = localRepository
        .getPileWithBatchesFlow(args.pile.pileId)
        .flowOn(IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val nonNullPileFlow = pileStateFlow
        .mapNotNull { it }
        .flowOn(IO)

    private val imagePathFlow = nonNullPileFlow
        .map { it.pile.pictureUri.path }
        .flowOn(IO)
        .distinctUntilChanged()

    val imageBitmapStateFlow = imagePathFlow.map(BitmapFactory::decodeFile)
        .flowOn(IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pileTitleFlow = nonNullPileFlow
        .map(PileWithBatches::pile / Pile::title)
        .flowOn(IO)
        .distinctUntilChanged()

    val barBatchWithBarsStateFlow = nonNullPileFlow
        .map(PileWithBatches::batchWithBars::get)
        .flowOn(IO)

    var allBarsFlow = nonNullPileFlow
        .map(PileWithBatches::bars::get)
        .flowOn(IO)
        .distinctUntilChanged()

    val evaluatedRowEntriesStateFlow = nonNullPileFlow
        .map(PileWithBatches::rowEvaluationEntries::get)
        .flowOn(IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    private val selectedBarIdsMutableStateFlow: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    val selectedBarIdsStateFlow: StateFlow<Set<String>> = selectedBarIdsMutableStateFlow.asStateFlow()

    private val selectedBarIds get() = selectedBarIdsStateFlow.value

    val selectedBarsStateFlow: StateFlow<List<Bar>> = combine(
        flow = selectedBarIdsMutableStateFlow,
        flow2 = allBarsFlow
    ) { ids, bars -> bars.filter { ids.contains(it.barId) } }
        .flowOn(IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())



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
    }.flowOn(IO).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    fun isBarSelected(bar: Bar) = selectedBarIds.contains(bar.barId)

    fun onImageClicked(bar: Bar?, point: PointF) {
        bar?.let {
            log("ON BAR CLICKED $bar")
            //selectedBarIdsMutableStateFlow.value = pileStateFlow.value!!.bars.findBarsNextTo(bar, 2).map { it.barId }.toSet()

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
            val averageBarDimensions = pile.bars.averageBarDimensions
            val rectToInsert = RectF(
                max(point.x - averageBarDimensions.width / 2, 0f),
                max(point.y - averageBarDimensions.height / 2, 0f),
                point.x + averageBarDimensions.width / 2,
                point.y + averageBarDimensions.height / 2
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

        selectedBars.let { bars ->
            val barsBetween = pileStateFlow.value?.bars?.findBarsBetween(bars[0], bars[1]) ?: emptyList()
            selectedBarIdsMutableStateFlow.value = selectedBarIds.toMutableSet().apply {
                addAll(barsBetween.map(Bar::barId))
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

    fun onShowRowMappingDialogClicked() = launch {
        navDispatcher.dispatch(NavigationEvent.FromDetailsToRowMappingDialog)
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
                localRepository.update(bar.copy(
                    rect = RectF(
                        bar.rect.left,
                        bar.rect.centerY() - (progress / 2f),
                        bar.rect.right,
                        bar.rect.centerY() + (progress / 2f)
                    )
                ))
            }
        }
    }

    fun onBatchLongClicked(batch: Batch?) {
        if(batch == null) return
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateToAddEditBatch(batch))
        }
    }

    fun areBarsInSameRow(selectedIds: Set<String>) : Boolean {
        if(selectedIds.size != 2) return false
        val ids = selectedIds.toList()
        return pileStateFlow.value?.bars?.areBarsInSameRow(ids[0], ids[1]) ?: false
    }


    fun onPileEvaluationDialogBackPressed(){
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onPileEvaluationExportButtonClicked(){
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateToExportPileEvaluationResult(pileStateFlow.value?.asPileEvaluation))
        }
    }
}