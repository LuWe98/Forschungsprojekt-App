package com.serverless.forschungsprojectfaas.model.room.junctions

import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.model.room.entities.BarBatch
import com.serverless.forschungsprojectfaas.model.room.entities.CapturedPicture

data class PictureWithBarBatches(
    @Embedded
    var picture: CapturedPicture,
    @Relation(
        entity = BarBatch::class,
        entityColumn = BarBatch.PICTURE_ID_COLUMN,
        parentColumn = CapturedPicture.ID_COLUMN
    )
    var batches: List<BarBatchWithBars>
)