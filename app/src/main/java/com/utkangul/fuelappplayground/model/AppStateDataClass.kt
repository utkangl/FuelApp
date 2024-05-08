package com.utkangul.fuelappplayground.model

import android.location.Location
import androidx.lifecycle.MutableLiveData

val currentAppState = MutableLiveData<AppStateDataClass>()

data class AppStateDataClass(
    val isLocationEnabled: Boolean = false,
    val isLocationAllowed: Boolean = false,
    var currentLocation: Location? = null
)

