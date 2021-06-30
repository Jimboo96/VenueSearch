package com.example.venuesearch.db

interface VenueSearcherPreferences {
    fun getLatestLatLng(): String?
    fun setLatestLatLng(latLng: String)
    fun locationUpdateIsReceived(): Boolean
}