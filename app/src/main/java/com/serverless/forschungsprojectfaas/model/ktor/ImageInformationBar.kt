package com.serverless.forschungsprojectfaas.model.ktor

import kotlinx.serialization.Serializable

@Serializable
data class ImageInformationBar(
    val caption: String,
    val bottom: Double,
    val left: Double,
    val right: Double,
    val top: Double
)