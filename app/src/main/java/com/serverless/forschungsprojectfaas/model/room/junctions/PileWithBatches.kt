package com.serverless.forschungsprojectfaas.model.room.junctions

import android.graphics.RectF
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.model.BoxDimension
import com.serverless.forschungsprojectfaas.model.EvaluatedPileResult
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

@Parcelize
data class PileWithBatches(
    @Embedded
    var pile: Pile,
    @Relation(
        entity = Bar::class,
        entityColumn = Bar.PILE_ID_COLUMN,
        parentColumn = Pile.ID_COLUMN
    )
    var barsWithBatches: List<BarWithBatch>
) : Parcelable {

    val bars get() = barsWithBatches.map(BarWithBatch::bar)

    val batches get() = barsWithBatches.map(BarWithBatch::batch).toSet()

    val batchWithBars
        get(): List<BatchWithBars> = run {
            barsWithBatches.groupBy {
                it.batch?.batchId
            }.map {
                BatchWithBars(
                    it.value.first().batch,
                    it.value.map(BarWithBatch::bar)
                )
            }
        }

    val averageBoxSize get(): BoxDimension = barsWithBatches.map(BarWithBatch::bar / Bar::rect).averageRectDimension


    fun findBarsInRow(
        bar: Bar,
        includeCallingBarInResult: Boolean = true,
        barsToIgnore: List<String> = emptyList()
    ): List<Bar> = findBarsLeftOf(bar).toMutableList().apply {
        addAll(findBarsRightOf(bar))
        if (includeCallingBarInResult) add(bar)
    }.filter {
        !barsToIgnore.contains(it.barId)
    }

    fun findBarsLeftOf(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findClosestLeftBar)

    fun findBarsRightOf(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findClosestRightBar)

    private fun findNearestBarsInLine(bar: Bar, receiveBarAction: ((Bar) -> (Bar?))): List<Bar> {
        var currentBar: Bar? = receiveBarAction(bar)
        return mutableListOf<Bar>().apply {
            while (currentBar != null) {
                add(currentBar!!)
                currentBar = receiveBarAction(currentBar!!)
            }
        }
    }

    fun findClosestLeftBar(bar: Bar): Bar? = bars.filter {
        bar.barId != it.barId
                && bar.rect.centerY() <= it.rect.bottom
                && bar.rect.centerY() >= it.rect.top
                && bar.rect.right > it.rect.right
    }.minByOrNull {
        (bar.rect.centerX() - it.rect.right).absoluteValue
    }

    fun findClosestRightBar(bar: Bar): Bar? = bars.filter {
        bar.barId != it.barId
                && bar.rect.centerY() <= it.rect.bottom
                && bar.rect.centerY() >= it.rect.top
                && bar.rect.left < it.rect.left
    }.minByOrNull {
        (bar.rect.centerX() - it.rect.left).absoluteValue
    }

    fun findClosestTopBar(bar: Bar, barsToIgnore: List<String> = emptyList()): Bar? = bars.filter {
        bar.barId != it.barId
                && !barsToIgnore.contains(it.barId)
                && bar.rect.bottom >= it.rect.bottom
    }.minByOrNull {
        (bar.rect.centerY() - it.rect.bottom).absoluteValue
    }

    fun findBarsBetween(barOne: Bar, barTwo: Bar): List<Bar> {
        val left: Bar
        val right: Bar
        if (barOne.rect.left < barTwo.rect.left) {
            left = barOne
            right = barTwo
        } else {
            left = barTwo
            right = barOne
        }
        return findBarsRightOf(left).let { rightOfLeftBars ->
            findBarsLeftOf(right).filter(rightOfLeftBars::contains)
        }
    }

    fun areBarsInSameRow(barIdOne: String, barIdTwo: String): Boolean {
        bars.let {
            return findBarsBetween(
                it.first { bar -> bar.barId == barIdOne },
                it.first { bar -> bar.barId == barIdTwo }
            ).isNotEmpty()
        }
    }

    fun getRowCount(): Int = bars.let {
        var rowCounter = 0
        val processedBars = mutableListOf<String>()
        var currentBar: Bar? = it.maxByOrNull(Bar::rect / RectF::bottom)

        while (currentBar != null) {
            rowCounter++

            findBarsInRow(
                bar = currentBar,
                barsToIgnore = processedBars
            ).let { barsOfRow ->
                processedBars.addAll(barsOfRow.map(Bar::barId))
            }
            currentBar = findClosestTopBar(
                bar = currentBar,
                barsToIgnore = processedBars
            )
        }

        rowCounter
    }

    fun getAllBarsMappedToRows(): List<List<Bar>> = bars.let {
        val rowList = mutableListOf<List<Bar>>()
        val processedBars = mutableListOf<String>()

        var currentBar: Bar? = it.maxByOrNull(Bar::rect / RectF::bottom)

        while (currentBar != null) {
            findBarsInRow(
                bar = currentBar,
                barsToIgnore = processedBars
            ).toSet().let { barsOfRow ->
                processedBars.addAll(barsOfRow.map(Bar::barId))
                rowList.add(barsOfRow.sortedBy(Bar::rect / RectF::left))
            }

            currentBar = findClosestTopBar(
                bar = currentBar,
                barsToIgnore = processedBars
            )
        }
        rowList
    }

    fun getBarRowWithBatch(): List<EvaluatedPileResult> = mutableListOf<EvaluatedPileResult>().apply {
        getAllBarsMappedToRows().forEachIndexed { row, barsOfRow ->
            batches.let { batches ->
                var currentBatchId: String? = null
                var lastColumn = 0

                barsOfRow.forEachIndexed { column, bar ->
                    if (column == 0) {
                        currentBatchId = bar.batchId
                    } else if (currentBatchId != bar.batchId || column == barsOfRow.size - 1) {
                        val batchOfGroup = batches.first { it?.batchId == barsOfRow[lastColumn].batchId }
                        EvaluatedPileResult(
                            row = row,
                            caption = batchOfGroup?.caption ?: "-",
                            amount = column - lastColumn,
                            fromColumn = lastColumn,
                            toColumn = column
                        ).let(::add)
                        lastColumn = column
                        currentBatchId = bar.batchId
                    }
                }
            }
        }
    }
}