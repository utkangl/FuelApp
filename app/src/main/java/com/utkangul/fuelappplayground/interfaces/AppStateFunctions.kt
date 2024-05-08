package com.utkangul.fuelappplayground.interfaces

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.OnMapReadyCallback
import com.utkangul.fuelappplayground.view.MainActivity

interface AppStateFunctions {
    fun isLocationEnabled(context: Context): Boolean
    fun isLocationAllowed(context: Context): Boolean
}