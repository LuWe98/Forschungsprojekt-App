package com.serverless.forschungsprojectfaas.model.ktor

import kotlinx.serialization.Serializable

@Serializable
data class ImageInformation(
    val name: String,
    val extension: String? = null,
    val image: String? = null,
    val time: Long = System.currentTimeMillis(),
    val result: List<ImageInformationBar> = emptyList(),
)