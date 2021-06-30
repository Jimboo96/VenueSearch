package com.example.venuesearch.data

interface VenueSearcherContract {

    interface View {

        /**
         * Notifies the [VenueSearcherRecyclerAdapter] about the updated [list].
         */
        fun updateVenueList(list: List<Venue>)

        /**
         * Hides the waiting for location view when the user has received the first location update.
         */
        fun hideWaitingForLocationView()

        /**
         * Shows the info text [visibility] when there is no venues found with the user's search word.
         * Hides the info text otherwise.
         */
        fun updateInfoTextVisibility(visibility: Int)

        /**
         * Updates the loading progress bar [visibility] when the search for Venues
         * is ongoing or completed.
         */
        fun updateLoadingProgressBarVisibility(visibility: Int)

        /**
         * Shows a Toast to the user when quota limit has been exceeded for the day.
         */
        fun showQuotaLimitToast()

        /**
         * Request fine location permission from the user.
         */
        fun requestLocationPermission()
    }

    interface Presenter {

        /**
         * Launches a job for searching venues via the FourSquare API with given [queryWord].
         * The venue list is updated if the user's location has been received at least once.
         */
        fun searchForVenues(queryWord: String)

        /**
         * Starts the location updates via the [CurrentLocationProvider].
         */
        fun startLocationUpdates()
    }
}