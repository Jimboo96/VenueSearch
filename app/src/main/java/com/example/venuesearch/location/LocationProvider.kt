package com.example.venuesearch.location

import android.location.Location

interface LocationProvider {

    /**
     * Starts the location updates if location is available.
     * Otherwise request for permissions is emitted.
     * @param locationListener is used for notifying the UI about received location update.
     */
    fun startLocationUpdates(locationListener: LocationUpdateReceivedListener)

    /**
     * Returns the last received LocationManager's [Location].
     */
    fun getCurrentLocation(): Location?

    /**
     * Checks if the user's [ACCESS_FINE_LOCATION] & [ACCESS_COARSE_LOCATION] permissions are granted
     * to determine whether or not to the location is available.
     */
    fun isLocationAvailable(): Boolean
}