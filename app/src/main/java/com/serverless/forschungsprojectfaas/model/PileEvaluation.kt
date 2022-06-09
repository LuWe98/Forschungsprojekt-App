package com.serverless.forschungsprojectfaas.model

import android.os.Parcelable
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import kotlinx.parcelize.Parcelize

@Parcelize
data class PileEvaluation(
    val pile: Pile,
    val rowEvaluationEntries: List<RowEvaluationEntry>
): Parcelable {

}