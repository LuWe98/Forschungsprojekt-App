package com.serverless.forschungsprojectfaas.dispatcher.selection

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.serverless.forschungsprojectfaas.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class OrderByItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<OrderByItem> {

    TITLE(
        textRes = R.string.title,
        iconRes = R.drawable.ic_text
    ),
    DATE(
        textRes = R.string.date,
        iconRes = R.drawable.ic_date
    ),
    NUMBER(
        textRes = R.string.stickCount,
        iconRes = R.drawable.ic_number
    )

}