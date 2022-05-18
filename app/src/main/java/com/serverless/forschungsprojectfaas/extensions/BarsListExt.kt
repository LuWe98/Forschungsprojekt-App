package com.serverless.forschungsprojectfaas.extensions


import android.graphics.RectF
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import kotlin.math.absoluteValue

//fun List<Bar>.findBarsInRow(
//    bar: Bar,
//    includeCallingBarInResult: Boolean = true,
//    barsToIgnore: List<String> = emptyList()
//): List<Bar> = findBarsLeftOf(bar).toMutableList().apply {
//    addAll(findBarsRightOf(bar))
//    if (includeCallingBarInResult) add(bar)
//}.filter {
//    !barsToIgnore.contains(it.barId)
//}
//
//fun List<Bar>.findBarsLeftOf(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findClosestLeftBar)
//
//fun List<Bar>.findBarsRightOf(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findClosestRightBar)
//
//private inline fun findNearestBarsInLine(bar: Bar, crossinline receiveBarAction: ((Bar) -> (Bar?))): List<Bar> {
//    var currentBar: Bar? = receiveBarAction(bar)
//    return mutableListOf<Bar>().apply {
//        while (currentBar != null) {
//            add(currentBar!!)
//            currentBar = receiveBarAction(currentBar!!)
//        }
//    }
//}
//
//fun List<Bar>.findClosestLeftBar(bar: Bar): Bar? = filter {
//    bar.barId != it.barId
//            && bar.rect.centerY() <= it.rect.bottom
//            && bar.rect.centerY() >= it.rect.top
//            && bar.rect.right > it.rect.right
//}.minByOrNull {
//    (bar.rect.centerX() - it.rect.right).absoluteValue
//}
//
//fun List<Bar>.findClosestRightBar(bar: Bar): Bar? = filter {
//    bar.barId != it.barId
//            && bar.rect.centerY() <= it.rect.bottom
//            && bar.rect.centerY() >= it.rect.top
//            && bar.rect.left < it.rect.left
//}.minByOrNull {
//    (bar.rect.centerX() - it.rect.left).absoluteValue
//}
//
//fun List<Bar>.findClosestTopBar(bar: Bar, barsToIgnore: List<String> = emptyList()): Bar? = filter {
//    bar.barId != it.barId
//            && !barsToIgnore.contains(it.barId)
//            && bar.rect.bottom >= it.rect.bottom
//}.minByOrNull {
//    (bar.rect.centerY() - it.rect.bottom).absoluteValue
//}
//
//fun List<Bar>.findBarsBetween(barOne: Bar, barTwo: Bar): List<Bar> {
//    val left: Bar
//    val right: Bar
//    if (barOne.rect.left < barTwo.rect.left) {
//        left = barOne
//        right = barTwo
//    } else {
//        left = barTwo
//        right = barOne
//    }
//    return findBarsRightOf(left).let { rightOfLeftBars ->
//        findBarsLeftOf(right).filter(rightOfLeftBars::contains)
//    }
//}
//
//fun List<Bar>.areBarsInSameRow(barIdOne: String, barIdTwo: String): Boolean {
//    return findBarsBetween(
//        first { bar -> bar.barId == barIdOne },
//        first { bar -> bar.barId == barIdTwo }
//    ).isNotEmpty()
//}
//
//fun List<Bar>.calculateRowCount(): Int {
//    var rowCounter = 0
//    val processedBars = mutableListOf<String>()
//    var currentBar: Bar? = maxByOrNull(Bar::rect / RectF::bottom)
//
//    while (currentBar != null) {
//        rowCounter++
//
//        findBarsInRow(
//            bar = currentBar,
//            barsToIgnore = processedBars
//        ).let { barsOfRow ->
//            processedBars.addAll(barsOfRow.map(Bar::barId))
//        }
//        currentBar = findClosestTopBar(
//            bar = currentBar,
//            barsToIgnore = processedBars
//        )
//    }
//
//    return rowCounter
//}
//
//fun List<Bar>.getAllBarsMappedToRows(): List<List<Bar>> {
//    val rowList = mutableListOf<List<Bar>>()
//    val processedBars = mutableListOf<String>()
//
//    var currentBar: Bar? = maxByOrNull(Bar::rect / RectF::bottom)
//
//    while (currentBar != null) {
//        findBarsInRow(
//            bar = currentBar,
//            barsToIgnore = processedBars
//        ).toSet().let { barsOfRow ->
//            processedBars.addAll(barsOfRow.map(Bar::barId))
//            rowList.add(barsOfRow.sortedBy(Bar::rect / RectF::left))
//        }
//
//        currentBar = findClosestTopBar(
//            bar = currentBar,
//            barsToIgnore = processedBars
//        )
//    }
//    return rowList
//}