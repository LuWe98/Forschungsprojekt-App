package com.serverless.forschungsprojectfaas.model.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(
    tableName = StickEntry.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = PictureEntry::class,
            parentColumns = [PictureEntry.ID_COLUMN],
            childColumns = [StickEntry.PICTURE_ENTRY_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class StickEntry(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = DESIGNATION_COLUMN)
    val designation: String,
    @ColumnInfo(name = PICTURE_ENTRY_ID_COLUMN)
    val pictureEntryId: String
) : RoomEntityMarker {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(StickEntry::id)

        const val TABLE_NAME = "StickEntryTable"

        const val ID_COLUMN = "stickId"
        const val DESIGNATION_COLUMN = "designation"
        const val PICTURE_ENTRY_ID_COLUMN = "pictureId"
    }
}