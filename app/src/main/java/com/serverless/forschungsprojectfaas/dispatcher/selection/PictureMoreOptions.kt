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
    DELETE(
        textRes = R.string.delete,
        iconRes = R.drawable.ic_delete
    )

}