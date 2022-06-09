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
    EXPORT(
        textRes = R.string.exportToCsv,
        iconRes = R.drawable.ic_insert_file
    ),
    DELETE(
        textRes = R.string.delete,
        iconRes = R.drawable.ic_delete
    )

}