package com.serverless.forschungsprojectfaas.model.room.entities

import android.graphics.RectF
import androidx.room.*
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(
    tableName = Bar.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = BarBatch::class,
            parentColumns = [BarBatch.ID_COLUMN],
            childColumns = [Bar.BATCH_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class Bar(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = UUID.randomUUID().toString(),
    @Embedded
    val rect: RectF,
    @ColumnInfo(name = BATCH_ID_COLUMN)
    val batchId: String
) : RoomEntityMarker {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Bar::id)

        const val TABLE_NAME = "BarTable"

        const val ID_COLUMN = "barId"
        const val BATCH_ID_COLUMN = "batchId"
    }
}