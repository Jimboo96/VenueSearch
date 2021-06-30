package com.example.venuesearch.data

import android.view.View
import androidx.annotation.VisibleForTesting
import com.example.venuesearch.db.VenueSearcherPreferences
import com.example.venuesearch.location.CurrentLocationProvider
import com.example.venuesearch.location.LocationUpdateReceivedListener
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

@VisibleForTesting
const val CODE_OK = 200

@VisibleForTesting
const val CODE_QUOTA_LIMIT = 429

class VenueSearcherPresenter(
    private val view: VenueSearcherContract.View,
    private val locationProvider: CurrentLocationProvider,
    private val api: VenueSearchApi,
    private val prefs: VenueSearcherPreferences
) : VenueSearcherContract.Presenter, LocationUpdateReceivedListener {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val list: ArrayList<Venue> = ArrayList()

    private lateinit var venueLoaderJob: Job

    init {
        startLocationUpdates()
    }

    override fun searchForVenues(queryWord: String) {
        list.removeAll(list)
        view.updateVenueList(list)

        view.updateInfoTextVisibility(View.GONE)

        cancelOngoingJobs()
        launchVenueLoaderJob(queryWord)
    }

    @VisibleForTesting
    fun launchVenueLoaderJob(queryWord: String) {
        venueLoaderJob = scope.launch {
            getLatLng()?.let { latLng ->
                view.updateLoadingProgressBarVisibility(View.VISIBLE)

                val venueList = getVenues(
                    latLng = latLng,
                    queryWord = queryWord
                )

                venueList?.let {
                    it.response.items.forEach { venueItem ->
                        val venue = Venue(
                            name = venueItem.name,
                            address = venueItem.location.address,
                            distance = venueItem.location.distance
                        )
                        list.add(venue)
                    }
                }

                view.updateLoadingProgressBarVisibility(View.GONE)

                if (list.isEmpty()) {
                    view.updateInfoTextVisibility(View.VISIBLE)
                }
            }

            view.updateVenueList(list.sortedBy { it.distance })
        }
    }

    @VisibleForTesting
    fun cancelOngoingJobs() {
        if (isVenueLoaderJobRunning()) {
            venueLoaderJob.cancel()
        }
    }

    @VisibleForTesting
    fun isVenueLoaderJobRunning(): Boolean {
        return ::venueLoaderJob.isInitialized && venueLoaderJob.isActive
    }

    @VisibleForTesting
    fun getLatLng(): String? {
        val latLng: String?
        val location = locationProvider.getCurrentLocation()
        if (location == null) {
            latLng = prefs.getLatestLatLng()
        } else {
            latLng = "${location.latitude},${location.longitude}"
            prefs.setLatestLatLng(latLng)
        }
        return latLng
    }

    @VisibleForTesting
    suspend fun getVenues(queryWord: String, latLng: String): VenueResponse? {
        return withContext(Dispatchers.IO) {
            val response = api.getVenuesByLocation(
                latLng = latLng,
                queryWord = queryWord
            ).execute()
            when (response.code()) {
                CODE_OK -> response.body()
                CODE_QUOTA_LIMIT -> {
                    view.showQuotaLimitToast()
                    null
                }
                else -> null
            }
        }
    }

    override fun startLocationUpdates() {
        locationProvider.startLocationUpdates(this)
    }

    override fun onLocationUpdateReceived() {
        if (!prefs.locationUpdateIsReceived()) {
            view.hideWaitingForLocationView()
        }
    }

    override fun onLocationPermissionRequested() {
        view.requestLocationPermission()
    }
}