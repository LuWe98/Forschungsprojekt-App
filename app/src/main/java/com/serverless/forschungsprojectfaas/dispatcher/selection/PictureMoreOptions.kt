package com.serverless.forschungsprojectfaas.dispatcher.selection

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.serverless.forschungsprojectfaas.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class PictureMoreOptions(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<PictureMoreOptions> {

    OPEN(
        textRes = R.string.open,
        iconRes = R.drawable.ic_open
    ),
    UPLOAD_CHANGES(
        textRes = R.string.uploadChanges,
        iconRes = R.drawable.ic_rotate_alt
    ),
    EVALUATE(
        textRes = R.string.evaluate,
        iconRes = R.drawable.ic_cloud_upload
    ),
    EXPORT(
        textRes = R.string.exportToCsv,
        iconRes = R.drawable.ic_insert_file
    ),
    DELETE(
        textRes = R.string.delete,
        iconRes = R.drawable.ic_delete
    );

    companion object {
        val notEvaluatedPileOptions: List<PictureMoreOptions> = listOf(EVALUATE, DELETE)
        val updatePileOptions: List<PictureMoreOptions> = listOf(OPEN, UPLOAD_CHANGES, EXPORT, DELETE)
    }
}