package com.utkangul.fuelappplayground.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.utkangul.fuelappplayground.R
import com.utkangul.fuelappplayground.model.Coordinates
import com.utkangul.fuelappplayground.model.PostJsonModel
import com.utkangul.fuelappplayground.model.car
import com.utkangul.fuelappplayground.model.currentAppState
import com.utkangul.fuelappplayground.model.destinationCoordinates
import com.utkangul.fuelappplayground.model.startCoordinates
import com.utkangul.fuelappplayground.viewModel.ApiViewModel
import com.utkangul.fuelappplayground.viewModel.MainViewModel
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val GPS_REQ_CODE = 100
    private val viewModel = MainViewModel()
    private val apiViewModel = ApiViewModel()
    private var listOfMarkers = ArrayList<Marker>()
    private var googleMap: GoogleMap? = null
    private var isMapExpanded = false
    private val final = Resources.getSystem().displayMetrics.heightPixels  // max pixels available
    private var currentHeight by Delegates.notNull<Int>()
    private lateinit var line: Polyline
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val fields = listOf(Place.Field.LAT_LNG)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        val autoCompleteSearchCV = findViewById<CardView>(R.id.autoCompleteSearchCV)
        val informationsCard = findViewById<CardView>(R.id.informationsCard)
        val mapFragmentContainer = findViewById<RelativeLayout>(R.id.mapFragmentContainer)
        val expandMapIV = findViewById<ImageView>(R.id.expandMapIV)


        // call enable function for gps if disabled
        if (!viewModel.isLocationEnabled(this)){ enableGPS() }

        // make request to grant location permissions if gps is enabled
        else if (viewModel.isLocationEnabled(this) && !viewModel.isLocationAllowed(this)){
            viewModel.requestLocationPermission(this,this)
        }

        mapFragment.getMapAsync(this)
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        currentHeight = mapFragmentContainer.layoutParams.height


         val intent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)

        // scale down the map view, add marker to selected place, draw the line and set zoom level
        val startAutoComplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val destinationPlace = Autocomplete.getPlaceFromIntent(intent)
                    destinationCoordinates = Coordinates(destinationPlace.latLng.latitude,destinationPlace.latLng.longitude)
                    addMarker("destination", destinationPlace.latLng!!, BitmapDescriptorFactory.HUE_GREEN)
                    slideResizeView(
                        mapFragmentContainer,final,currentHeight,300,
                        onAnimationStart = {
                           apiViewModel.calculateCost(PostJsonModel(startCoordinates, destinationCoordinates,car)) },
                        onAnimationEnd = {
                            autoCompleteSearchCV.visibility = View.VISIBLE
                            drawLineBetweenMarkers()
                            zoomToFitMarkers()
                            informationsCard.visibility = View.VISIBLE
                            expandMapIV.visibility = View.VISIBLE
                        }
                    )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                slideResizeView(
                    mapFragmentContainer,final,currentHeight,300,
                    onAnimationStart = { },
                    onAnimationEnd = {autoCompleteSearchCV.visibility = View.VISIBLE; informationsCard.visibility = View.VISIBLE;expandMapIV.visibility = View.VISIBLE}
                )
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show()
            }
        }


        expandMapIV.setOnClickListener {
            if (!isMapExpanded){
                slideResizeView(mapFragmentContainer, currentHeight, final, 300, onAnimationStart = { autoCompleteSearchCV.visibility = View.INVISIBLE; informationsCard.visibility = View.GONE ; expandMapIV.visibility = View.VISIBLE}, onAnimationEnd = {  })
                isMapExpanded = true
            } else {
                isMapExpanded = false
                slideResizeView(
                    mapFragmentContainer,final,currentHeight,300,
                    onAnimationStart = { },
                    onAnimationEnd = {autoCompleteSearchCV.visibility = View.VISIBLE; informationsCard.visibility = View.VISIBLE;}
                )
            }
        }
        // expand the map view and launch places autocomplete
        autoCompleteSearchCV.setOnClickListener {
            slideResizeView(
                mapFragmentContainer, currentHeight, final, 300,
                onAnimationStart = { autoCompleteSearchCV.visibility = View.INVISIBLE; informationsCard.visibility = View.GONE;  expandMapIV.visibility = View.GONE},
                onAnimationEnd = { startAutoComplete.launch(intent) }
            )
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage("Do You Want To Exit?")
                builder.setPositiveButton("Yes") { _, _ -> finish() }
                builder.setNegativeButton("No", null)
                builder.create().show()
            }
        }
        this.onBackPressedDispatcher.addCallback(callback)

    } // end of onCreate



    override fun onMapReady(p0: GoogleMap) {
        println("map ready")
        googleMap = p0
        zoomToCurrentLocation()
    }

    // show a dialog that asks user to enable location
    private fun enableGPS() {

        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)
            .setFastestInterval(1000)

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        settingsBuilder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this)
            .checkLocationSettings(settingsBuilder.build())

        result.addOnSuccessListener {
            println("GPS enabled.")

        }

        result.addOnFailureListener { ex ->
            println("Error: GPS could not enabled.")

            if (ex is ApiException) {
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvableApiException = ex as ResolvableApiException
                            resolvableApiException.startResolutionForResult(this, GPS_REQ_CODE)
                        } catch (e: IntentSender.SendIntentException) {
                            println("catch, while trying gps enable")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        println("LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE")
                    }
                }
            }
        }
    }

    // Manages result of enableGPS function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    onMapReady(googleMap!!)
                    Toast.makeText(this, "GPS enabled", Toast.LENGTH_SHORT).show()
                    if (!viewModel.isLocationAllowed(this)){
                        viewModel.requestLocationPermission(this,this)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "You denied enabling GPS. GPS service is required for this application. Please turn it on", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Manages what happens after location permissions are asked
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(googleMap!!)
            } else {
                Toast.makeText(this, "Location permission is required for map functionality.", Toast.LENGTH_LONG).show()
            }
        }
    }


    // view functions
    private fun slideResizeView(view: View, currentHeight: Int, newHeight: Int, duration: Long, onAnimationEnd: () -> Unit, onAnimationStart: () -> Unit) {
        val slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(duration)

        slideAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }

        val animationSet = AnimatorSet()
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.play(slideAnimator)
        animationSet.addListener(object : Animator.AnimatorListener {
            // invoke the function from the parameter
            override fun onAnimationEnd(animation: Animator) { onAnimationEnd.invoke() }

            override fun onAnimationStart(animation: Animator) {
                onAnimationStart.invoke()
            }

            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationCancel(animation: Animator) {}

        })
        animationSet.start()
    }

    private fun zoomToCurrentLocation(){
        viewModel.getCurrentLocation(this, this, fusedLocationProviderClient, this)
        println(viewModel.isLocationEnabled(this))
        println(viewModel.isLocationAllowed(this))
        // if location is enabled and permission is granted, get current location and zoom to it
        if (viewModel.isLocationEnabled(this) && viewModel.isLocationAllowed(this)) {
            println("Location Permissions Successfully Granted")
            currentAppState.observeForever {  state ->
                when (state.currentLocation != null) {
                    true ->{
                        currentAppState.value?.currentLocation?.let {
                            zoomToPointOnMap(it.latitude, it.longitude)
                            val currentLatLng = LatLng(it.latitude,it.longitude)
                            addMarker("source",currentLatLng,BitmapDescriptorFactory.HUE_RED)
                        }
                    }
                    false -> println("current location may be null")
                }
            }
        }
    }

    private fun zoomToPointOnMap(lat: Double, lng: Double){
        val latLng = LatLng(lat,lng)
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
        googleMap!!.animateCamera(CameraUpdateFactory.zoomIn())
        googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(15F), 2000, null)
    }

    private fun addMarker(title: String, position: LatLng, color: Float) {
        val options = MarkerOptions().position(position).title(title).icon(BitmapDescriptorFactory.defaultMarker(color))
        if (listOfMarkers.size>1){
            listOfMarkers[1].remove();listOfMarkers.removeLast();line.remove()
        }

        val marker = googleMap?.addMarker(options)
        listOfMarkers.add(marker!!)
    }

    private fun zoomToFitMarkers() {
        val boundsBuilder = LatLngBounds.Builder()
        for (m in listOfMarkers) { boundsBuilder.include(m.position) }
        val bounds = boundsBuilder.build()
        val paddingFromEdgeAsPX = 100
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, paddingFromEdgeAsPX)
        googleMap?.animateCamera(cu)
    }

    private fun drawLineBetweenMarkers(){
        val point1 = LatLng(listOfMarkers[0].position.latitude,listOfMarkers[0].position.longitude)
        val point2 = LatLng(listOfMarkers[1].position.latitude,listOfMarkers[1].position.longitude)
        line = googleMap!!.addPolyline(PolylineOptions()
            .add(point1, point2)
            .width(3f)
            .color(Color.RED))
        zoomToFitMarkers()
    }

}