package com.example.venuesearch.data

import com.google.gson.annotations.SerializedName

data class Venue(
    var name: String = "",
    var address: String? = "",
    var distance: Int = 0
)

data class VenueResponse(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("response") val response: Response
)

data class Meta(
    @SerializedName("code") val resultCode: Int,
    @SerializedName("requestId") val requestId: String
)

data class Response(
    @SerializedName("venues") val items: List<VenueItem>
)

data class VenueItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("location") val location: VenueLocation
)

data class VenueLocation(
    @SerializedName("address") val address: String?,
    @SerializedName("distance") val distance: Int
)