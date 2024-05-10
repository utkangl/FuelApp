package com.utkangul.fuelappplayground.interfaces

import android.content.Context

interface IAppStateFunctions {
    fun isLocationEnabled(context: Context): Boolean
    fun isLocationAllowed(context: Context): Boolean
}