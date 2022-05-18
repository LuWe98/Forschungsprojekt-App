package com.serverless.forschungsprojectfaas.model.room.entities

import android.graphics.Color
import androidx.room.*
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.random.Random

@Parcelize
@Entity(
    tableName = Batch.TABLE_NAME,
    indices = [
        Index(
            value = [Batch.CAPTION_COLUMN],
            unique = true
        )
    ]
)
data class Batch(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val batchId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = CAPTION_COLUMN)
    val caption: String,
    @ColumnInfo(name = COLOR_COLUMN)
    val colorInt: Int = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)),
) : RoomEntityMarker {
    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Batch::caption)

        const val TABLE_NAME = "BatchTable"
        const val ID_COLUMN = "batchId"
        const val CAPTION_COLUMN = "caption"
        const val COLOR_COLUMN = "color"
    }
}