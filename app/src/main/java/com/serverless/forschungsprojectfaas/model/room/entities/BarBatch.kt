package com.serverless.forschungsprojectfaas.model.room.entities

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.random.Random

@Parcelize
@Entity(
    tableName = BarBatch.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = CapturedPicture::class,
            parentColumns = [CapturedPicture.ID_COLUMN],
            childColumns = [BarBatch.PICTURE_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ])
data class BarBatch(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = CAPTION_COLUMN)
    val caption: String,
    @ColumnInfo(name = COLOR_COLUMN)
    val colorInt: Int = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)),
    @ColumnInfo(name = PICTURE_ID_COLUMN)
    val pictureId: String
): RoomEntityMarker {
    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(BarBatch::caption)

        const val TABLE_NAME = "BarBatchTable"
        const val ID_COLUMN = "barId"
        const val CAPTION_COLUMN = "caption"
        const val COLOR_COLUMN = "color"
        const val PICTURE_ID_COLUMN = "pictureId"
    }
}