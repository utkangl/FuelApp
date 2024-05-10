package com.utkangul.fuelappplayground.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


lateinit var startCoordinates: Coordinates
lateinit var destinationCoordinates: Coordinates
         var car: String = "sedan"

@JsonClass(generateAdapter = true)
data class PostJsonModel(
    @Json(name = "start") val start: Coordinates,
    @Json(name = "target") val target: Coordinates,
    @Json(name = "car") val car: String
)


data class Coordinates(
    @Json(name = "lat") val latitude: Double,
    @Json(name = "lon") val longitude: Double
)


