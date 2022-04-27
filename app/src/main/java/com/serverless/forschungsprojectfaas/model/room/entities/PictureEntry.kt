package com.serverless.forschungsprojectfaas.model.room.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.EvaluationStatus
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = PictureEntry.TABLE_NAME,
    indices = [
        Index(value = [PictureEntry.TITLE_COLUMN], unique = true)
    ]
)
@Parcelize
data class PictureEntry(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,
    @ColumnInfo(name = TIMESTAMP_COLUMN)
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = COMPRESSED_PICTURE_COLUMN)
    val pictureUri: Uri,
    @ColumnInfo(name = EVALUATION_STATUS_COLUMN)
    val evaluationStatus: EvaluationStatus = EvaluationStatus.NOT_EVALUATED
) : RoomEntityMarker {

    val timeStampAsDate get() = SimpleDateFormat.getDateInstance().format(Date(timestamp)).toString()

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(PictureEntry::id)

        const val TABLE_NAME = "PictureTable"

        const val ID_COLUMN = "pictureId"
        const val TITLE_COLUMN = "title"
        const val TIMESTAMP_COLUMN = "timestamp"
        const val COMPRESSED_PICTURE_COLUMN = "picture"
        const val EVALUATION_STATUS_COLUMN = "evaluationStatus"
    }

}
