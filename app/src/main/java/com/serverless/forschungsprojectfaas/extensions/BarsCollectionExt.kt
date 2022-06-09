package com.serverless.forschungsprojectfaas.extensions

import android.graphics.PointF
import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.BarRowColumnInfo
import com.serverless.forschungsprojectfaas.model.BoxDimensions
import com.serverless.forschungsprojectfaas.model.RowEvaluationEntry
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import kotlin.math.absoluteValue

typealias BarId = String

val List<Bar>.averageBarDimensions get(): BoxDimensions = map(Bar::rect).averageRectDimension

fun List<Bar>.findBarsInRow(
    bar: Bar,
    includeCallingBarInResult: Boolean = true,
    barsToIgnore: List<BarId> = emptyList()
): List<Bar> = findBarsLeftOf(bar).toMutableList().apply {
    addAll(this@findBarsInRow.findBarsRightOf(bar))
    if (includeCallingBarInResult) add(bar)
}.filter {
    !barsToIgnore.contains(it.barId)
}

fun List<Bar>.findBarsLeftOf(bar: Bar, limit: Int = Int.MAX_VALUE): List<Bar> = findNearestBarsInLine(bar, limit, ::findClosestLeftBar)

fun List<Bar>.findBarsRightOf(bar: Bar, limit: Int = Int.MAX_VALUE): List<Bar> = findNearestBarsInLine(bar, limit, ::findClosestRightBar)

private inline fun findNearestBarsInLine(bar: Bar, limit: Int, crossinline receiveBarAction: ((Bar) -> (Bar?))): List<Bar> {
    if (limit <= 0) return emptyList()
    var currentBar: Bar? = receiveBarAction(bar)
    return mutableListOf<Bar>().apply {
        while (currentBar != null) {
            add(currentBar!!)
            if (size == limit) break
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

fun List<Bar>.findNextTopBar(bar: Bar, barsToIgnore: List<BarId> = emptyList()): Bar? = filter {
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

fun List<Bar>.findClosestBottomBar(pointF: PointF): Bar? = filter {
    pointF.y <= it.rect.centerY()
            && pointF.x <= it.rect.right
            && pointF.x >= it.rect.left
}.minByOrNull {
    (pointF.y - it.rect.top).absoluteValue
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

fun List<Bar>.areBarsInSameRow(barIdOne: BarId, barIdTwo: BarId): Boolean = findBarsBetween(
    first { bar -> bar.barId == barIdOne },
    first { bar -> bar.barId == barIdTwo }
).isNotEmpty()

val List<Bar>.rowCount
    get(): Int {
        var rowCounter = 0
        val processedBars = mutableListOf<BarId>()
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
        val processedBars = mutableListOf<BarId>()

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

fun List<Bar>.asEvaluatedRowEntries(batches: Collection<Batch?>): List<RowEvaluationEntry> {
    val results = mutableListOf<RowEvaluationEntry>()
    mappedToRows.forEachIndexed { row, barsOfRow ->
        batches.let { batches ->
            var currentBatchId: String? = null
            var lastColumn = 0

            barsOfRow.forEachIndexed { column, bar ->
                if (column == 0) {
                    currentBatchId = bar.batchId
                } else if (currentBatchId != bar.batchId || column == barsOfRow.size - 1) {
                    val batchOfGroup = batches.firstOrNull { it?.batchId == barsOfRow[lastColumn].batchId }
                    RowEvaluationEntry(
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

/**
 * Gibt die Zeile der Bar zurück, in welcher sich diese befindet.
 */
fun List<Bar>.findRowCountOfBar(barId: BarId): Int? {
    mappedToRows.forEachIndexed { row, bars ->
        if (bars.any { it.barId == barId }) {
            return row
        }
    }
    return null
}

/**
 * Wird verwendet, um einfach auf die Spalten und Zeilen Informationen einer Bar zugreifen zu können.
 * Die Informationen erhält man über die ID der Bar
 */
val List<Bar>.asBarRowColumnMap
    get() : HashMap<BarId, BarRowColumnInfo> = run {
        val barRowColumnMap = HashMap<BarId, BarRowColumnInfo>()
        mappedToRows.forEachIndexed { row, bars ->
            bars.forEachIndexed { column, bar ->
                barRowColumnMap[bar.barId] = BarRowColumnInfo(bar, row, column)
            }
        }
        barRowColumnMap
    }


fun List<Bar>.filterOverlayingBars() = filter { bar ->
    val intersectingBars = filter {
        bar.barId != it.barId && bar.rect.intersectingArea(it.rect) > 0f
    }.map {
        Pair(it.barId, bar.rect.intersectingRect(it.rect)!!)
    }
    val intersectingArea = intersectingBars.sumOf(Pair<BarId, RectF>::second / RectF::area)
    val processedIds = mutableListOf<BarId>()
    val duplicateIntersectingArea = intersectingBars.sumOf { (id, rect) ->
        intersectingBars.filter { (filterId, _) ->
            id != filterId && !processedIds.contains(filterId)
        }.sumOf {
            rect.intersectingArea(it.second)
        }.also {
            processedIds.add(id)
        }
    }

    (intersectingArea - duplicateIntersectingArea) < bar.rect.area * 0.85
}

//TODO -> Finds bar, which are isolated
fun List<Bar>.filterIsolatedBars(averageBarDimensions: BoxDimensions = this.averageBarDimensions): List<Bar> {
    val searchRect = RectF()
    return filter { bar ->
        searchRect.set(
            bar.rect.left - averageBarDimensions.width * 1.25f,
            bar.rect.top - averageBarDimensions.height * 1.25f,
            bar.rect.right + averageBarDimensions.width * 1.25f,
            bar.rect.bottom + averageBarDimensions.height * 1.25f
        )
        any { bar.barId != it.barId && searchRect.containsPartial(it.rect) }
    }
}

fun List<Bar>.findHorizontalBarHoles(averageBarDimensions: BoxDimensions = this.averageBarDimensions) {
    val candidates = mapNotNull { bar ->
        findClosestLeftBar(bar)?.let { leftBar ->
            val spaceBetween = bar.left - leftBar.right
            val isValid = spaceBetween > averageBarDimensions.width
                    && (
                    findClosestBottomBar(PointF(leftBar.right + spaceBetween / 2f, leftBar.bottom)) != null
                            || findClosestBottomBar(PointF(leftBar.right, bar.bottom)) != null
                            || findClosestBottomBar(PointF(bar.left, bar.bottom)) != null
                    )
            if (!isValid) return@mapNotNull null
            Pair(leftBar, bar)
        }
    }

    //TODO -> Weiter
}

fun List<Bar>.fixBarDimensions(averageBarDimensions: BoxDimensions = this.averageBarDimensions): List<Bar> = onEach {
    if (it.rect.width() > averageBarDimensions.width * 1.05) {
        val nearestLeftBar = findClosestLeftBar(it)
        val nearestRightBar = findClosestRightBar(it)
        val distanceToLeftBar = it.rect.left - (nearestLeftBar?.rect?.right ?: it.rect.left)
        val distanceToRightBar = it.rect.right - (nearestRightBar?.rect?.left ?: it.rect.right)

        val sizeDiff = it.rect.width() - averageBarDimensions.width
        if (distanceToLeftBar < distanceToRightBar) {
            it.setBounds(left = it.rect.left + sizeDiff)
        } else {
            it.setBounds(right = it.rect.right - sizeDiff)
        }
    }

    if (it.rect.height() < averageBarDimensions.height * 0.95) {
        val nearestTopBar = findClosestTopBar(it)
        val nearestBottomBar = findClosestBottomBar(it)
        val distanceToTopBar = it.rect.top - (nearestTopBar?.rect?.bottom ?: it.rect.top)
        val distanceToBottomBar = it.rect.bottom - (nearestBottomBar?.rect?.top ?: it.rect.bottom)

        val sizeDiff = it.rect.height() - averageBarDimensions.height
        if (distanceToTopBar > distanceToBottomBar) {
            it.setBounds(top = it.rect.top + sizeDiff)
        } else {
            it.setBounds(bottom = it.rect.bottom - sizeDiff)
        }
    }
}

fun List<Bar>.findBarsNextTo(bar: Bar, limit: Int): List<Bar> = mutableListOf<Bar>().apply {
    addAll(this@findBarsNextTo.findBarsLeftOf(bar, limit))
    addAll(this@findBarsNextTo.findBarsRightOf(bar, limit))
}

//TODO -> Adjusted die Batches um einen herum
fun List<Bar>.adjustBatches(limit: Int = 2, acceptanceThreshold: Float = 0.5f, addNullAsValid: Boolean = false): List<Bar> {
    //TODO -> Schauen, ob um ein bar herum auch null BatchIDs sind, wenn ja werden diese nicht gezählt
    val results = this.toMutableList()
    var isListDifferent = true

    do {
        val adjustedBars: List<Bar> = results.map { bar ->
            val barsNextToBar = results.findBarsNextTo(bar, limit)

            val groupedBars = barsNextToBar.groupBy {
                it.batchId
            }.map {
                it.value
            }

            groupedBars.maxByOrNull {
                it.size
            }?.let { majorityBars ->
                val sampleBar = majorityBars.first()
                if (sampleBar.batchId == null || bar.batchId == sampleBar.batchId) return@let

                val fraction = if(addNullAsValid) {
                    val nullBatchIdsSize = groupedBars.firstOrNull { it.firstOrNull()?.batchId == null }?.size ?: 0
                    (majorityBars.size + nullBatchIdsSize).toFloat() / barsNextToBar.size.toFloat()
                } else {
                    majorityBars.size.toFloat() / barsNextToBar.size.toFloat()
                }

                if(acceptanceThreshold == 1f) {
                    if (fraction == acceptanceThreshold || (bar.batchId == null && fraction == acceptanceThreshold)) {
                        return@map bar.copy(batchId = sampleBar.batchId)
                    }
                } else {
                    if (fraction > acceptanceThreshold || (bar.batchId == null && fraction >= acceptanceThreshold)) {
                        return@map bar.copy(batchId = sampleBar.batchId)
                    }
                }
            }
            bar
        }
        log("DIFF: ${(results - adjustedBars.toSet()).size} WITH LIMIT: $limit")
        if(results == adjustedBars) {
            isListDifferent = false
        } else {
            results.setItems(adjustedBars)
        }
    } while (isListDifferent)

    return results
}