package com.serverless.forschungsprojectfaas.model.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.extensions.asEvaluatedRowEntries
import com.serverless.forschungsprojectfaas.model.PileEvaluation
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import kotlinx.parcelize.Parcelize

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

    val rowEvaluationEntries get() = bars.asEvaluatedRowEntries(batches)

    val asPileEvaluation get(): PileEvaluation = PileEvaluation(pile, rowEvaluationEntries)

}