package com.serverless.forschungsprojectfaas.model

import com.serverless.forschungsprojectfaas.model.room.entities.Bar

data class BarRowColumnInfo(
    val bar: Bar,
    val row: Int,
    val column: Int
)