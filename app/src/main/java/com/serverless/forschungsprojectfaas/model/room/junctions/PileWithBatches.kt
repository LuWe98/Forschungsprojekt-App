package com.serverless.forschungsprojectfaas.model.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.extensions.sumOf
import com.serverless.forschungsprojectfaas.model.BoxDimension
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import kotlinx.parcelize.Parcelize
import kotlin.math.absoluteValue

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
): Parcelable {

    val bars get() = barsWithBatches.map(BarWithBatch::bar)

    val batches
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

    val averageBoxSize get(): BoxDimension = run {
        val avgWidth = barsWithBatches.sumOf { it.bar.rect.width() } / barsWithBatches.size
        val avgHeight = barsWithBatches.sumOf { it.bar.rect.height() } / barsWithBatches.size
        BoxDimension(avgWidth, avgHeight)
    }


    fun findBarsInLine(bar: Bar): List<Bar> = findNearestLeftBars(bar).toMutableList().apply {
        addAll(findNearestRightBars(bar))
    }

    fun findNearestLeftBars(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findNearestLeftBar)

    fun findNearestRightBars(bar: Bar): List<Bar> = findNearestBarsInLine(bar, ::findNearestRightBar)

    private fun findNearestBarsInLine(bar: Bar, receiveBarAction: ((Bar) -> (Bar?))): List<Bar> {
        var currentBar: Bar? = receiveBarAction(bar)
        return mutableListOf<Bar>().apply {
            while (currentBar != null) {
                add(currentBar!!)
                currentBar = receiveBarAction(currentBar!!)
            }
        }
    }

    fun findNearestLeftBar(bar: Bar): Bar? = bars.filter {
        bar.barId != it.barId
                && bar.rect.centerY() <= it.rect.bottom
                && bar.rect.centerY() >= it.rect.top
                && bar.rect.right > it.rect.right
    }.minByOrNull {
        (bar.rect.left - it.rect.right).absoluteValue
    }

    fun findNearestRightBar(bar: Bar): Bar? = bars.filter {
        bar.barId != it.barId
                && bar.rect.centerY() <= it.rect.bottom
                && bar.rect.centerY() >= it.rect.top
                && bar.rect.left < it.rect.left
    }.minByOrNull {
        (bar.rect.right - it.rect.left).absoluteValue
    }

}