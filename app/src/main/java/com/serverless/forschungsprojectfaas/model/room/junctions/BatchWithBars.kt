package com.serverless.forschungsprojectfaas.model.room.junctions

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.extensions.div
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch

data class BatchWithBars(
    @Embedded
    var batch: Batch?,
    @Relation(
        entity = Bar::class,
        entityColumn = Bar.BATCH_ID_COLUMN,
        parentColumn = Batch.ID_COLUMN
    )
    var bars: List<Bar>
) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BatchWithBars>() {
            override fun areContentsTheSame(oldItem: BatchWithBars, newItem: BatchWithBars) = oldItem == newItem
            override fun areItemsTheSame(oldItem: BatchWithBars, newItem: BatchWithBars) = oldItem.batch?.batchId == newItem.batch?.batchId
        }
    }
}