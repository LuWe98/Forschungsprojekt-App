package com.serverless.forschungsprojectfaas.extensions

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.serverless.forschungsprojectfaas.model.BarRowColumnInfo
import com.serverless.forschungsprojectfaas.model.BoxDimensions
import com.serverless.forschungsprojectfaas.model.RowEvaluationEntry
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import kotlin.math.absoluteValue

typealias BarId = String
typealias BatchId = String

val List<Bar>.averageBarDimensions
    get() = BoxDimensions(
        width = sumOf(Bar::width) / size,
        height = sumOf(Bar::height) / size
    )

fun List<Bar>.findBarsInRow(
    bar: Bar,
    includeCallingBarInResult: Boolean = true,
    barsToIgnore: List<BarId> = emptyList()
): List<Bar> = findBarsLeftOf(bar).reversed().toMutableList().let { bars ->
    if (includeCallingBarInResult) bars.add(bar)
    bars.addAll(this@findBarsInRow.findBarsRightOf(bar))
    if (barsToIgnore.isEmpty()) bars else bars.filter { !barsToIgnore.contains(it.barId) }
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
            && bar.centerY <= it.bottom
            && bar.centerY >= it.top
            && bar.right > it.right
}.minByOrNull {
    (bar.centerX - it.right).absoluteValue
}

fun List<Bar>.findClosestRightBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.centerY <= it.bottom
            && bar.centerY >= it.top
            && bar.left < it.left
}.minByOrNull {
    (bar.centerX - it.left).absoluteValue
}

fun List<Bar>.findClosestBarLeftOfWithSameBatch(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.batchId == it.batchId
            && bar.centerY <= it.bottom
            && bar.centerY >= it.top
            && bar.right > it.right
}.minByOrNull {
    (bar.centerX - it.right).absoluteValue
}

fun List<Bar>.findNextTopBar(bar: Bar, barsToIgnore: List<BarId> = emptyList()): Bar? = filter {
    bar.barId != it.barId
            && !barsToIgnore.contains(it.barId)
            && bar.bottom >= it.bottom
}.minByOrNull {
    (bar.centerY - it.bottom).absoluteValue
}

fun List<Bar>.findClosestTopBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.bottom >= it.bottom
            && bar.top >= it.centerY
            && bar.centerX <= it.right
            && bar.centerX >= it.left
}.minByOrNull {
    (bar.centerY - it.bottom).absoluteValue
}

fun List<Bar>.findClosestBottomBar(bar: Bar): Bar? = filter {
    bar.barId != it.barId
            && bar.top <= it.top
            && bar.bottom <= it.centerY
            && bar.centerX <= it.right
            && bar.centerX >= it.left
}.minByOrNull {
    (bar.centerY - it.top).absoluteValue
}

fun List<Bar>.findClosestBottomBar(pointF: PointF): Bar? = filter {
    pointF.y <= it.centerY
            && pointF.x <= it.right
            && pointF.x >= it.left
}.minByOrNull {
    (pointF.y - it.top).absoluteValue
}

fun List<Bar>.findBarsBetween(barOne: Bar, barTwo: Bar): List<Bar> {
    val left: Bar
    val right: Bar
    if (barOne.left < barTwo.left) {
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

fun List<Bar>.findBarsToSideOf(bar: Bar, limit: Int): List<Bar> = findBarsLeftOf(bar, limit).toMutableList().addAllChain(findBarsRightOf(bar, limit))

fun List<Bar>.findBarsInsideBounds(bounds: RectF) = filter { bounds.isIntersecting(it.rect) }

fun List<Bar>.areBarsInSameRow(barIdOne: BarId, barIdTwo: BarId): Boolean = findBarsBetween(
    first { it.barId == barIdOne },
    first { it.barId == barIdTwo }
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

val List<Bar>.mappedToMutableRows get(): MutableList<MutableList<Bar>> = mappedToRows.map(List<Bar>::toMutableList).toMutableList()

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


// ---------------- Ab hier kommen die Filterungen um Ergebnisse zu Fixen im Pile ---------------------------------------

/**
 * Filters overlapping bars
 */
fun List<Bar>.filterOverlappingBars(@FloatRange(from = 0.0, to = 1.0) percentage: Float = 0.85f) = filter { bar ->
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

    (intersectingArea - duplicateIntersectingArea) < bar.rect.area * percentage
}

/**
 * Filters isolated bars
 */
fun List<Bar>.filterIsolatedBars(averageBarDimensions: BoxDimensions = this.averageBarDimensions): List<Bar> = RectF().let { searchRect ->
    filter { bar ->
        searchRect.set(
            bar.left - averageBarDimensions.width * 1.25f,
            bar.top - averageBarDimensions.height * 1.25f,
            bar.right + averageBarDimensions.width * 1.25f,
            bar.bottom + averageBarDimensions.height * 1.25f
        )
        any { bar.barId != it.barId && searchRect.isIntersecting(it.rect) }
    }
}

/**
 * Fixes dimensions of bars
 */
fun List<Bar>.fixBarDimensions(
    averageBarDimensions: BoxDimensions = this.averageBarDimensions
): List<Bar> = onEach { bar ->
    if (bar.width > averageBarDimensions.width * 1.05) {
        val nearestLeftBar = findClosestLeftBar(bar)
        val nearestRightBar = findClosestRightBar(bar)
        val distanceToLeftBar = bar.left - (nearestLeftBar?.right ?: bar.left)
        val distanceToRightBar = bar.right - (nearestRightBar?.left ?: bar.right)

        val sizeDiff = bar.width - averageBarDimensions.width
        if (distanceToLeftBar < distanceToRightBar) {
            bar.setBounds(left = bar.left + sizeDiff)
        } else {
            bar.setBounds(right = bar.right - sizeDiff)
        }
    }

    if (bar.height < averageBarDimensions.height * 0.95) {
        val nearestTopBar = findClosestTopBar(bar)
        val nearestBottomBar = findClosestBottomBar(bar)
        val distanceToTopBar = bar.top - (nearestTopBar?.bottom ?: bar.top)
        val distanceToBottomBar = bar.bottom - (nearestBottomBar?.top ?: bar.bottom)

        val sizeDiff = bar.height - averageBarDimensions.height
        if (distanceToTopBar > distanceToBottomBar) {
            bar.setBounds(top = bar.top + sizeDiff)
        } else {
            bar.setBounds(bottom = bar.bottom - sizeDiff)
        }
    }
}

/**
 * Adjusts the batches if right and left are the same BatchIds
 */
fun List<Bar>.adjustBatchIdsIfPossible(
    @IntRange(from = 1, to = 10) lookAheadOnEachSide: Int = 2,
    @FloatRange(from = 0.0, to = 1.0) acceptanceThreshold: Float = 0.5f,
    valueNullAsValid: Boolean = false
): List<Bar> {
    val rowBars: MutableList<MutableList<Bar>> = mappedToMutableRows
    val batchIdsNextToBar = mutableListOf<BatchId?>()
    var wasSomethingChanged = true

    while (wasSomethingChanged) {
        wasSomethingChanged = false

        for (rowIndex in 0 until rowBars.size) {
            val columnCount = rowBars[rowIndex].size

            for (columnIndex in 0 until columnCount) {
                batchIdsNextToBar.clear()

                for (shift in 1..lookAheadOnEachSide) {
                    //TODO -> Hier noch check machen. Wenn bar mehr als X entfernt ist, dann wird es nicht dazu gezählt.
                    // if(rowBars[rowIndex][columnIndex - shift].right - bar.left < X)
                    if (columnIndex - shift >= 0) {
                        batchIdsNextToBar.add(rowBars[rowIndex][columnIndex - shift].batchId)
                    }
                    //TODO -> Hier noch check machen. Wenn bar mehr als X entfernt ist, dann wird es nicht dazu gezählt.
                    // if(rowBars[rowIndex][columnIndex + shift].left - bar.right < X)
                    if (columnIndex + shift < columnCount) {
                        batchIdsNextToBar.add(rowBars[rowIndex][columnIndex + shift].batchId)
                    }
                }

                val batchIdCountMap = batchIdsNextToBar.groupingBy { it }.eachCount()

                batchIdCountMap.maxByOrNull(Map.Entry<BatchId?, Int>::value)?.let {
                    val bar: Bar = rowBars[rowIndex][columnIndex]

                    if (it.key == null || bar.batchId == it.key) return@let

                    val mostCommonBarsProportion: Float = if (valueNullAsValid) {
                        (it.value + (batchIdCountMap[null] ?: 0)).toFloat() / batchIdsNextToBar.size.toFloat()
                    } else {
                        it.value.toFloat() / batchIdsNextToBar.size.toFloat()
                    }

                    if (acceptanceThreshold == 1f
                        && mostCommonBarsProportion == acceptanceThreshold
                        || (bar.batchId == null && mostCommonBarsProportion == acceptanceThreshold)
                    ) {
                        rowBars[rowIndex][columnIndex] = bar.copy(batchId = it.key)
                        wasSomethingChanged = true
                    } else if (mostCommonBarsProportion > acceptanceThreshold
                        || (bar.batchId == null && mostCommonBarsProportion >= acceptanceThreshold)
                    ) {
                        rowBars[rowIndex][columnIndex] = bar.copy(batchId = it.key)
                        wasSomethingChanged = true
                    }
                }
            }
        }
    }

    return rowBars.flatten()
}


/**
 * Findet Bereiche im Pile, wo rechts und links n gleiche Bars sind. Ist das der Fall, werden die BatchIDs der Bars zwischendrin ausgetauscht.
 */
fun List<Bar>.adjustSpacesBetweenBatchGroups(
    @IntRange(from = 0, to = 10) minBatchAppearanceOnEachSide: Int = 4
): List<Bar> = mappedToMutableRows.also { rowBars ->
    for (rowIndex in 0 until rowBars.size) {
        val columnCount = rowBars[rowIndex].size
        outer@ for (columnIndex in 0 until columnCount) {
            val bar = rowBars[rowIndex][columnIndex]
            if (columnIndex + minBatchAppearanceOnEachSide > columnCount - 1
                || columnIndex - minBatchAppearanceOnEachSide - 1 < 0
                || rowBars[rowIndex][columnIndex + 1].batchId == bar.batchId
            ) continue@outer

            for (leftLookahead in 1 until minBatchAppearanceOnEachSide) {
                if (rowBars[rowIndex][columnIndex - leftLookahead].batchId != bar.batchId) continue@outer
            }

            //Hier wird jetzt versucht, von einem validen Bar, die nächsten Einträge zu finden zwischendrin
            // --> Den nächsten block mit bspw 3 finden, um das zwischendrin zu konvertieren.
            inner@ for (index in columnIndex + 1 until columnCount) {
                if (index > columnCount - minBatchAppearanceOnEachSide) break@inner
                if (rowBars[rowIndex][index].batchId != bar.batchId) continue@inner

                //Schaut in n nach rechts um zu schauen, ob mindestens diese n den gleichen Batch haben
                for (rightLookahead in 1 until minBatchAppearanceOnEachSide) {
                    if (rowBars[rowIndex][index + rightLookahead].batchId != bar.batchId) continue@inner
                }

                //Hier dann festgestellt, dass es welche nebendran gibt
                for (columnsToChange in columnIndex + 1 until index) {
                    rowBars[rowIndex][columnsToChange] = rowBars[rowIndex][columnsToChange].copy(batchId = bar.batchId)
                }
            }
        }
    }
}.flatten()


/**
 * Schaut auf jede Seite eines Bars und wenn links und rechts davon mindestens n gleiche von einer und n gleiche von einer anderen Farbe sind, wird geschaut was besser passt.
 */
fun List<Bar>.adjustLonelyBarsBetween(
    @IntRange(from = 1, to = 10) lookAheadOnEachSide: Int = 3,
    @FloatRange(from = 0.0, to = 1.0) minMostCommonBarPortion: Float = 0.75f,
    batchMap: Map<BatchId, Batch>
): List<Bar> {
    val rowBars: MutableList<MutableList<Bar>> = mappedToMutableRows
    val batchIdsNextToBar = mutableListOf<BatchId?>()
    var wasSomethingChanged = true

    while (wasSomethingChanged) {
        wasSomethingChanged = false

        for (rowIndex in 0 until rowBars.size) {
            val columnCount = rowBars[rowIndex].size

            for (columnIndex in 0 until columnCount) {
                batchIdsNextToBar.clear()

                for (shift in 1..lookAheadOnEachSide) {
                    if (columnIndex - shift >= 0) {
                        batchIdsNextToBar.add(rowBars[rowIndex][columnIndex - shift].batchId)
                    }
                    if (columnIndex + shift < columnCount) {
                        batchIdsNextToBar.add(rowBars[rowIndex][columnIndex + shift].batchId)
                    }
                }

                batchIdsNextToBar
                    .groupingBy { it }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }.let { batchIdCountMap: List<Pair<BatchId?, Int>> ->
                        if (batchIdCountMap.size < 2) return@let
                        val mostCommonBatchInfo = batchIdCountMap[0]
                        if (mostCommonBatchInfo.second < lookAheadOnEachSide) return@let
                        val secondBatchInfo = batchIdCountMap[1]
                        if ((secondBatchInfo.second + mostCommonBatchInfo.second).toFloat() / batchIdsNextToBar.size.toFloat() < minMostCommonBarPortion) return@let

                        val bar: Bar = rowBars[rowIndex][columnIndex]
                        val barBatch: Batch = batchMap[bar.batchId] ?: return@let
                        val firstBatch: Batch = batchMap[mostCommonBatchInfo.first] ?: return@let
                        val secondBatch: Batch = batchMap[secondBatchInfo.first] ?: return@let

                        if (barBatch.caption[0] == firstBatch.caption[0]
                            && barBatch.caption[0] != secondBatch.caption[0]
                            && firstBatch.batchId != bar.batchId
                        ) {
                            rowBars[rowIndex][columnIndex] = bar.copy(batchId = firstBatch.batchId)
                            wasSomethingChanged = true
                        } else if (barBatch.caption[0] == secondBatch.caption[0]
                            && barBatch.caption[0] != firstBatch.caption[0]
                            && secondBatch.batchId != bar.batchId
                        ) {
                            rowBars[rowIndex][columnIndex] = bar.copy(batchId = secondBatch.batchId)
                            wasSomethingChanged = true
                        } else if (barBatch.caption[1] == firstBatch.caption[1]
                            && barBatch.caption[1] != secondBatch.caption[1]
                            && firstBatch.batchId != bar.batchId
                        ) {
                            rowBars[rowIndex][columnIndex] = bar.copy(batchId = firstBatch.batchId)
                            wasSomethingChanged = true
                        } else if (barBatch.caption[1] == secondBatch.caption[1]
                            && barBatch.caption[1] != firstBatch.caption[1]
                            && secondBatch.batchId != bar.batchId
                        ) {
                            rowBars[rowIndex][columnIndex] = bar.copy(batchId = secondBatch.batchId)
                            wasSomethingChanged = true
                        }
                    }
            }
        }
    }

    return rowBars.flatten()
}





//Schaut nach links und es reicht aber, dass andere Bars mit gleichen Batches gefunden werden, egal ob die dazwischen
// -> BSP: A A A A B C Z H G N N N N
// Alle Labels dazwischen werden ausgetauscht, da es unwahrscheinlich ist, dass dazwischen noch andere sind.
// Könnte aber auch fehler herbeirufen, wenn dazwischen welche sind, die wenig sind. Mal schauen

fun List<Bar>.findAndFillSpacesBetweenDiff(minBarCountOnEachSide: Int = 4): List<Bar> {
    return emptyList()
}



//TODO -> Finds horizontal Holes in Pile -> Diese sollen dann aufgefüllt werden
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