package com.utkangul.fuelapp.interfaces

import android.content.Context

interface IAppStateFunctions {
    fun isLocationEnabled(context: Context): Boolean
    fun isLocationAllowed(context: Context): Boolean
}