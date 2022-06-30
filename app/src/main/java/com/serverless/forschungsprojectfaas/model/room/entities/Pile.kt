package com.serverless.forschungsprojectfaas.model.room.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.serverless.forschungsprojectfaas.dispatcher.selection.PictureMoreOptions
import com.serverless.forschungsprojectfaas.extensions.asBase64String
import com.serverless.forschungsprojectfaas.extensions.fileExtension
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.extensions.loadBitmap
import com.serverless.forschungsprojectfaas.model.PileStatus
import com.serverless.forschungsprojectfaas.model.PileStatus.*
import com.serverless.forschungsprojectfaas.model.ktor.ImageInformation
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = Pile.TABLE_NAME,
    indices = [
        Index(
            value = [Pile.TITLE_COLUMN],
            unique = true
        )
    ]
)
@Parcelize
data class Pile(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val pileId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,
    @ColumnInfo(name = TIMESTAMP_COLUMN)
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = PICTURE_URI_COLUMN)
    val pictureUri: Uri,
    @ColumnInfo(name = EVALUATION_STATUS_COLUMN)
    val pileStatus: PileStatus = NOT_EVALUATED
) : RoomEntityMarker {

    val timeStampAsDateString get() = SimpleDateFormat.getDateInstance().format(Date(timestamp)).toString()

    val bitmap get(): Bitmap? = pictureUri.path?.let(BitmapFactory::decodeFile)

    val moreOptionsSelectionOptions get(): List<PictureMoreOptions> = when(pileStatus) {
        FAILED, NOT_EVALUATED -> PictureMoreOptions.notEvaluatedPileOptions
        LOCALLY_CHANGED, UPLOADED -> PictureMoreOptions.updatePileOptions
        EVALUATING -> emptyList()
    }

    fun asImageInformationRequest(fallbackFileExtension: String = "png") = ImageInformation(
        name = title,
        extension = pictureUri.fileExtension ?: fallbackFileExtension,
        image = bitmap?.asBase64String(),
    )

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Pile::pileId)

        const val TABLE_NAME = "PileTable"

        const val ID_COLUMN = "pileId"
        const val TITLE_COLUMN = "title"
        const val TIMESTAMP_COLUMN = "timestamp"
        const val PICTURE_URI_COLUMN = "picture"
        const val EVALUATION_STATUS_COLUMN = "evaluationStatus"
    }

}