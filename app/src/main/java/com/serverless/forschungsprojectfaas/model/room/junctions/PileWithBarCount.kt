package com.serverless.forschungsprojectfaas.model.room.junctions

import androidx.room.Embedded
import com.serverless.forschungsprojectfaas.extensions.div
import com.serverless.forschungsprojectfaas.extensions.generateDiffItemCallback
import com.serverless.forschungsprojectfaas.model.room.entities.Pile

data class PileWithBarCount(
    @Embedded
    val pile: Pile,
    val count: Int
) {
    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(PileWithBarCount::pile / Pile::pileId)
    }
}