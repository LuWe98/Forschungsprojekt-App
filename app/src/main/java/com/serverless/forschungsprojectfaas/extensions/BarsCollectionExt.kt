package com.serverless.forschungsprojectfaas.extensions

import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.BoxDimension
import com.serverless.forschungsprojectfaas.model.EvaluatedPileResult
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import kotlin.math.absoluteValue

val List<Bar>.averageBarDimensions get(): BoxDimension = map(Bar::rect).averageRectDimension

fun List<Bar>.findBarsInRow(
    bar: Bar,
    includeCallingBarInResult: Boolean = true,
    barsToIgnore: List<String> = emptyList()
): List<Bar> = findBarsLeftOf(bar).toMutableList().apply {
    addAll(this@findBarsInRow.findBarsRightOf(bar))
    if (includeCallingBarInResult) add(bar)
}.filter {
    !barsToIgnore.contains(it.barId)
}

fun List<Bar>.findBarsLeftOf(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findClosestLeftBar)

fun List<Bar>.findBarsRightOf(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findClosestRightBar)

private inline fun findNearestBarsInLine(bar: Bar, crossinline receiveBarAction: ((Bar) -> (Bar?))): List<Bar> {
    var currentBar: Bar? = receiveBarAction(bar)
    return mutableListOf<Bar>().apply {
        while (currentBar != null) {
            add(currentBar!!)
            currentBar = receiveBarAction(currentBar!!)
        }
    }
}

fun List<Bar>.findClosestLeftBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.rect.centerY() <= it.rect.bottom
            && bar.rect.centerY() >= it.rect.top
            && bar.rect.right > it.rect.right
}.minByOrNull {
    (bar.rect.centerX() - it.rect.right).absoluteValue
}

fun List<Bar>.findClosestRightBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.rect.centerY() <= it.rect.bottom
            && bar.rect.centerY() >= it.rect.top
            && bar.rect.left < it.rect.left
}.minByOrNull {
    (bar.rect.centerX() - it.rect.left).absoluteValue
}

fun List<Bar>.findNextTopBar(bar: Bar, barsToIgnore: List<String> = emptyList()): Bar? = filter {
    bar.barId != it.barId
            && !barsToIgnore.contains(it.barId)
            && bar.rect.bottom >= it.rect.bottom
}.minByOrNull {
    (bar.rect.centerY() - it.rect.bottom).absoluteValue
}

fun List<Bar>.findClosestTopBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.rect.bottom >= it.rect.bottom
            && bar.rect.top >= it.rect.centerY()
            && bar.rect.centerX() <= it.rect.right
            && bar.rect.centerX() >= it.rect.left
}.minByOrNull {
    (bar.rect.centerY() - it.rect.bottom).absoluteValue
}

fun List<Bar>.findClosestBottomBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.rect.top <= it.rect.top
            && bar.rect.bottom <= it.rect.centerY()
            && bar.rect.centerX() <= it.rect.right
            && bar.rect.centerX() >= it.rect.left
}.minByOrNull {
    (bar.rect.centerY() - it.rect.top).absoluteValue
}

fun List<Bar>.findBarsBetween(barOne: Bar, barTwo: Bar): List<Bar> {
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

fun List<Bar>.areBarsInSameRow(barIdOne: String, barIdTwo: String): Boolean = findBarsBetween(
    first { bar -> bar.barId == barIdOne },
    first { bar -> bar.barId == barIdTwo }
).isNotEmpty()

val List<Bar>.rowCount
    get(): Int {
        var rowCounter = 0
        val processedBars = mutableListOf<String>()
        var currentBar: Bar? = maxByOrNull(Bar::rect / RectF::bottom)

        while (currentBar != null) {
            rowCounter++

            findBarsInRow(
                bar = currentBar,
                barsToIgnore = processedBars
            ).let { barsOfRow ->
                processedBars.addAll(barsOfRow.map(Bar::barId))
            }
            currentBar = findNextTopBar(
                bar = currentBar,
                barsToIgnore = processedBars
            )
        }

        return rowCounter
    }

val List<Bar>.mappedToRows
    get(): List<List<Bar>> {
        val rowList = mutableListOf<List<Bar>>()
        val processedBars = mutableListOf<String>()

        var currentBar: Bar? = maxByOrNull(Bar::rect / RectF::bottom)

        while (currentBar != null) {
            findBarsInRow(
                bar = currentBar,
                barsToIgnore = processedBars
            ).toSet().let { barsOfRow ->
                processedBars.addAll(barsOfRow.map(Bar::barId))
                rowList.add(barsOfRow.sortedBy(Bar::rect / RectF::left))
            }

            currentBar = findNextTopBar(
                bar = currentBar,
                barsToIgnore = processedBars
            )
        }
        return rowList
    }

fun List<Bar>.asEvaluatedPileResult(batches: Collection<Batch?>): List<EvaluatedPileResult> {
    val results = mutableListOf<EvaluatedPileResult>()
    mappedToRows.forEachIndexed { row, barsOfRow ->
        batches.let { batches ->
            var currentBatchId: String? = null
            var lastColumn = 0

            barsOfRow.forEachIndexed { column, bar ->
                if (column == 0) {
                    currentBatchId = bar.batchId
                } else if (currentBatchId != bar.batchId || column == barsOfRow.size - 1) {
                    val batchOfGroup = batches.firstOrNull { it?.batchId == barsOfRow[lastColumn].batchId }
                    EvaluatedPileResult(
                        row = row,
                        caption = batchOfGroup?.caption ?: "-",
                        amount = column - lastColumn,
                        fromColumn = lastColumn,
                        toColumn = column
                    ).let(results::add)
                    lastColumn = column
                    currentBatchId = bar.batchId
                }
            }
        }
    }
    return results
}