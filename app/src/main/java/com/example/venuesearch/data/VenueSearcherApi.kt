package com.example.venuesearch.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

//TODO: Add your own CLIENT_ID & CLIENT_SECRET!
private const val CLIENT_ID = "YOUR_CLIENT_ID"
private const val CLIENT_SECRET = "YOUR_CLIENT_SECRET"
private const val FOURSQUARE_VERSION = 20210628
private const val VENUE_QUERY_LIMIT = 20

interface VenueSearchApi {

    /**
     * Fetches venues by given location [latLng] & [queryWord] via FourSquare Api.
     */
    @Headers("Accept: application/json")
    @GET("v2/venues/search")
    fun getVenuesByLocation(
        @Query("client_id") clientId: String = CLIENT_ID,
        @Query("client_secret") clientSecret: String = CLIENT_SECRET,
        @Query("v") version: Int = FOURSQUARE_VERSION,
        @Query("ll") latLng: String,
        @Query("query") queryWord: String,
        @Query("limit") limit: Int = VENUE_QUERY_LIMIT
    ): Call<VenueResponse>
}