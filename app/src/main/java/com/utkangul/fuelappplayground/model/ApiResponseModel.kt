package com.utkangul.fuelappplayground.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ApiResponseModel(
    @Json(name = "cost") val cost: Float,
    @Json(name = "unit") val unit: String
)

