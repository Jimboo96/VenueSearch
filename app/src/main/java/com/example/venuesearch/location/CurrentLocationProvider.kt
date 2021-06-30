package com.example.venuesearch.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat

@VisibleForTesting
const val MINIMUM_LOCATION_UPDATE_INTERVAL_MS = 10000L

@VisibleForTesting
const val MINIMUM_LOCATION_UPDATE_DISTANCE_METERS = 1F

class CurrentLocationProvider(
    private val context: Context,
    private val locationManager: LocationManager
) : LocationProvider, LocationListener {

    private lateinit var locationUpdateReceivedListener: LocationUpdateReceivedListener
    private var currentLocation: Location? = null

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(locationListener: LocationUpdateReceivedListener) {
        this.locationUpdateReceivedListener = locationListener
        if (isLocationAvailable()) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_LOCATION_UPDATE_INTERVAL_MS,
                MINIMUM_LOCATION_UPDATE_DISTANCE_METERS,
                this
            )
        } else {
            locationUpdateReceivedListener.onLocationPermissionRequested()
        }
    }

    override fun getCurrentLocation(): Location? {
        return currentLocation
    }

    override fun isLocationAvailable(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_DENIED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_DENIED
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        locationUpdateReceivedListener.onLocationUpdateReceived()
    }
}