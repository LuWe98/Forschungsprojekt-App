package com.serverless.forschungsprojectfaas.model.room.junctions

import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.extensions.div
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.BarBatch

data class BarBatchWithBars(
    @Embedded
    var barBatch: BarBatch,
    @Relation(
        entity = Bar::class,
        entityColumn = Bar.BATCH_ID_COLUMN,
        parentColumn = BarBatch.ID_COLUMN
    )
    var bars: List<Bar>
) {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(BarBatchWithBars::barBatch / BarBatch::id)
    }

}