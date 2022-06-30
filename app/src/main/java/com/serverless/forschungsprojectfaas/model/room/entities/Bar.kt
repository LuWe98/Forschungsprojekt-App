package com.serverless.forschungsprojectfaas.model.room.entities

import android.graphics.RectF
import androidx.room.*
import com.serverless.forschungsprojectfaas.extensions.area
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
            onDelete = ForeignKey.SET_NULL
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
    val batchId: String? = null,
    @ColumnInfo(name = PILE_ID_COLUMN)
    val pileId: String,
    @Embedded
    val rect: RectF,
) : RoomEntityMarker {

    val height get() = rect.height()
    val width get() = rect.width()
    val centerX get() = rect.centerX()
    val centerY get() = rect.centerY()
    val area get() = rect.area
    val left get() = rect.left
    val right get() = rect.right
    val top get() = rect.top
    val bottom get() = rect.bottom

    fun setBounds(left: Float = rect.left,
                  top: Float = rect.top,
                  right: Float = rect.right,
                  bottom: Float = rect.bottom) {
        rect.set(left, top, right, bottom)
    }

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Bar::barId)

        const val TABLE_NAME = "BarTable"

        const val ID_COLUMN = "barId"
        const val BATCH_ID_COLUMN = "batchId"
        const val PILE_ID_COLUMN = "pileId"
    }
}