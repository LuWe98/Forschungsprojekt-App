package com.serverless.forschungsprojectfaas.dispatcher.selection

import android.os.Parcelable
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback

interface SelectionTypeItemMarker<T: Enum<T>> : Parcelable {

    val textRes: Int
    val iconRes: Int

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(SelectionTypeItemMarker<*>::textRes)
    }

}