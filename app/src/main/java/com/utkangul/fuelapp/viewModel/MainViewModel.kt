package com.utkangul.fuelapp.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.LocationResult

import com.utkangul.fuelapp.interfaces.IAppStateFunctions
import com.utkangul.fuelapp.interfaces.ILocationFunctions
import com.utkangul.fuelapp.model.AppStateDataClass
import com.utkangul.fuelapp.model.Coordinates
import com.utkangul.fuelapp.model.currentAppState
import com.utkangul.fuelapp.model.startCoordinates
import com.utkangul.fuelapp.view.MainActivity

class MainViewModel: ViewModel(), ILocationFunctions, IAppStateFunctions {

    // ask user for location permission
    override fun requestLocationPermission(context: Context, activity: MainActivity) {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(activity, permissions, 1)

            return
        }
    }

    // return true if device's location is enabled
    override fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    override fun getCurrentLocation(context: Context, activity: MainActivity, fusedLocationClient: FusedLocationProviderClient, onMapReadyCallback: OnMapReadyCallback) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000) // 10 seconds, adjust as needed
                .setFastestInterval(5000) // 5 seconds, adjust as needed

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    for (location in p0.locations) {
                        println("Location found: $location")
                        currentAppState.postValue(AppStateDataClass(currentLocation = location))
                        startCoordinates = Coordinates(location.latitude,location.longitude)
                        fusedLocationClient.removeLocationUpdates(this) // Stop listening for updates once location is found
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            Toast.makeText(context, "Location permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // return true if location permissions are granted
    override fun isLocationAllowed(context: Context): Boolean{
        val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }


}