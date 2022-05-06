package com.serverless.forschungsprojectfaas.model.room.junctions

import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Pile

data class PileWithBatches(
    @Embedded
    var pile: Pile,
    @Relation(
        entity = Bar::class,
        entityColumn = Bar.PILE_ID_COLUMN,
        parentColumn = Pile.ID_COLUMN
    )
    var barsWithBatches: List<BarWithBatch>
) {
    val batches
        get(): List<BatchWithBars> = run {
            barsWithBatches.groupBy {
                it.batch.batchId
            }.map {
                BatchWithBars(
                    it.value.first().batch,
                    it.value.map(BarWithBatch::bar)
                )
            }
        }
}