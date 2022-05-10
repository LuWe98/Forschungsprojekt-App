package com.serverless.forschungsprojectfaas.model.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import kotlinx.parcelize.Parcelize

@Parcelize
data class BarWithBatch(
    @Embedded
    var bar: Bar,
    @Relation(
        entity = Batch::class,
        entityColumn = Batch.ID_COLUMN,
        parentColumn = Bar.BATCH_ID_COLUMN
    )
    var batch: Batch?,
): Parcelable