package com.utkangul.fuelapp.interfaces

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.OnMapReadyCallback
import com.utkangul.fuelapp.view.MainActivity

interface ILocationFunctions {
    fun requestLocationPermission(context: Context, activity: MainActivity)
    //fun requestEnableLocation(context: Context) : Unit
    fun getCurrentLocation(p0: Context, p1: MainActivity, p2: FusedLocationProviderClient, p3: OnMapReadyCallback)
}