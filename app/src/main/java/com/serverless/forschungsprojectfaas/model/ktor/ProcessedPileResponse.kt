package com.serverless.forschungsprojectfaas.model.ktor

import com.serverless.forschungsprojectfaas.extensions.sumOf
import com.serverless.forschungsprojectfaas.model.BoxDimensions
import kotlinx.serialization.Serializable

@Serializable
data class ProcessedPileResponse(
    val result: List<PotentialBox>
) {
    val averageBoxDimensions
        get() = BoxDimensions(
            width = result.sumOf(PotentialBox::width) / result.size,
            height = result.sumOf(PotentialBox::height) / result.size
        )
}