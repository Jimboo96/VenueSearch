package com.example.venuesearch.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

private const val SHARED_PREFS_NAME = "shared_prefs"
private const val KEY_LATEST_LAT_LNG = "latest_lat_lng"

class SharedPrefs(context: Context) : VenueSearcherPreferences {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)

    override fun getLatestLatLng(): String? {
        return prefs.getString(KEY_LATEST_LAT_LNG, null)
    }

    override fun setLatestLatLng(latLng: String) {
        prefs.edit().putString(KEY_LATEST_LAT_LNG, latLng).apply()
    }

    override fun locationUpdateIsReceived(): Boolean {
        return getLatestLatLng() != null
    }
}