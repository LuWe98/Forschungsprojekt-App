package com.serverless.forschungsprojectfaas.model.room.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = PictureEntry.TABLE_NAME,
    indices = [
        Index(value = [PictureEntry.NAME_COLUMN], unique = true)
    ]
)
@Parcelize
data class PictureEntry(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = NAME_COLUMN)
    val name: String,
    @ColumnInfo(name = TIMESTAMP_COLUMN)
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = COMPRESSED_PICTURE_COLUMN)
    val pictureUri: Uri
) : RoomEntityMarker {

    val timeStampAsDate get() = SimpleDateFormat.getDateInstance().format(Date(timestamp)).toString()

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(PictureEntry::id)

        const val TABLE_NAME = "PictureTable"

        const val ID_COLUMN = "pictureId"
        const val NAME_COLUMN = "name"
        const val TIMESTAMP_COLUMN = "timestamp"
        const val COMPRESSED_PICTURE_COLUMN = "picture"
    }

}
