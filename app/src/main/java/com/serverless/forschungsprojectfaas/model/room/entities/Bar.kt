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
            entity = Batch::class,
            parentColumns = [Batch.ID_COLUMN],
            childColumns = [Bar.BATCH_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Pile::class,
            parentColumns = [Pile.ID_COLUMN],
            childColumns = [Bar.PILE_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [Bar.BATCH_ID_COLUMN]),
        Index(value = [Bar.PILE_ID_COLUMN])
    ]
)
@Parcelize
data class Bar(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val barId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = BATCH_ID_COLUMN)
    val batchId: String,
    @ColumnInfo(name = PILE_ID_COLUMN)
    val pileId: String,
    @Embedded
    val rect: RectF,
) : RoomEntityMarker {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Bar::barId)

        const val TABLE_NAME = "BarTable"

        const val ID_COLUMN = "barId"
        const val BATCH_ID_COLUMN = "batchId"
        const val PILE_ID_COLUMN = "pileId"
    }
}