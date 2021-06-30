package com.example.venuesearch.data

import android.location.Location
import com.example.venuesearch.db.VenueSearcherPreferences
import com.example.venuesearch.location.CurrentLocationProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback

class VenueSearcherPresenterTest {

    @MockK
    private lateinit var venueSearchView: VenueSearcherContract.View

    @MockK
    private lateinit var currentLocationProvider: CurrentLocationProvider

    @MockK
    private lateinit var api: VenueSearchApi

    @MockK
    private lateinit var prefs: VenueSearcherPreferences

    @MockK
    private lateinit var mockLocation: Location

    private lateinit var presenter: VenueSearcherPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { currentLocationProvider.startLocationUpdates(any()) } returns Unit
        every { currentLocationProvider.getCurrentLocation() } returns mockLocation
        every { mockLocation.latitude } returns 10.012
        every { mockLocation.longitude } returns 11.055
        every { prefs.setLatestLatLng(any()) } returns Unit

        every { venueSearchView.hideWaitingForLocationView() } returns Unit
        every { venueSearchView.requestLocationPermission() } returns Unit
        every { venueSearchView.showQuotaLimitToast() } returns Unit
        every { venueSearchView.updateLoadingProgressBarVisibility(any()) } returns Unit
        every { venueSearchView.updateInfoTextVisibility(any()) } returns Unit
        every { venueSearchView.updateVenueList(any()) } returns Unit

        every {
            api.getVenuesByLocation(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns getMockVenueResponse(CODE_OK)

        presenter = VenueSearcherPresenter(venueSearchView, currentLocationProvider, api, prefs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Location updates are started when Presenter is initialized`() {
        verify(exactly = 1) { currentLocationProvider.startLocationUpdates(any()) }
        confirmVerified(currentLocationProvider)
    }

    @Test
    fun `Waiting for location view is hidden when the first location update is received`() {
        // Arrange
        every { prefs.locationUpdateIsReceived() } returns false

        // Act
        presenter.onLocationUpdateReceived()

        // Assert
        verify(exactly = 1) {
            prefs.locationUpdateIsReceived()
            venueSearchView.hideWaitingForLocationView()
        }
        confirmVerified(prefs, venueSearchView)
    }

    @Test
    fun `Waiting for location view is not hidden when location update is already received`() {
        // Arrange
        every { prefs.locationUpdateIsReceived() } returns true

        // Act
        presenter.onLocationUpdateReceived()

        // Assert
        verify(exactly = 0) {
            venueSearchView.hideWaitingForLocationView()
        }
        verify(exactly = 1) {
            prefs.locationUpdateIsReceived()
        }
        confirmVerified(venueSearchView, prefs)
    }

    @Test
    fun `View is notified when location permission is requested`() {
        // Act
        presenter.onLocationPermissionRequested()

        // Assert
        verify(exactly = 1) { venueSearchView.requestLocationPermission() }
        confirmVerified(venueSearchView)
    }

    @Test
    fun `LatLng string is returned when a LatLng value is stored in SharedPreferences and LocationProvider returns null`() {
        // Arrange
        val expected = "10.123,11.123"
        every { currentLocationProvider.getCurrentLocation() } returns null
        every { prefs.getLatestLatLng() } returns expected

        // Act
        val result = presenter.getLatLng()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `getLatLng() returns null when no location update is received or stored in SharedPreferences`() {
        // Arrange
        every { currentLocationProvider.getCurrentLocation() } returns null
        every { prefs.getLatestLatLng() } returns null

        // Act
        val result = presenter.getLatLng()

        // Assert
        assertNull(result)
    }

    @Test
    fun `LatLng string is returned and SharedPreferences are updated when LocationProvider returns a proper current location value`() {
        // Arrange
        val expected = "10.012,11.055"

        // Act
        val result = presenter.getLatLng()

        // Assert
        verify(exactly = 1) {
            prefs.setLatestLatLng(expected)
        }
        confirmVerified(prefs)
        assertEquals(expected, result)
    }

    @Test
    fun `Ongoing jobs are cancelled when cancelOngoingJobs() is called`() {
        // Arrange
        presenter.launchVenueLoaderJob("")

        // Act
        presenter.cancelOngoingJobs()
        val result = presenter.isVenueLoaderJobRunning()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `getVenues() returns VenueResponse via FourSquare API using given queries`() {
        // Arrange
        val latLng = "10.1522,11.2545"
        val queryWord = "coffee"
        val expectedSize = 3

        // Act
        val result = runBlocking {
            presenter.getVenues(queryWord, latLng)
        }

        // Assert
        assertEquals(expectedSize, result!!.response.items.size)
        assertEquals("Imaginary land 1", result.response.items[0].location.address)
        coVerify(exactly = 1) {
            api.getVenuesByLocation(any(), any(), any(), latLng, queryWord, any())
        }
        confirmVerified(api)
    }

    @Test
    fun `getVenues() returns null and notifies view to show a toast when quota limit has been exceeded`() {
        // Arrange
        val latLng = "10.1522,11.2545"
        val queryWord = "coffee"
        every {
            api.getVenuesByLocation(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns getMockVenueResponse(CODE_QUOTA_LIMIT)

        // Act
        val result = runBlocking {
            presenter.getVenues(queryWord, latLng)
        }

        // Assert
        assertNull(result)
        coVerify(exactly = 1) {
            api.getVenuesByLocation(any(), any(), any(), latLng, queryWord, any())
            venueSearchView.showQuotaLimitToast()
        }
        confirmVerified(api, venueSearchView)
    }

    @Test
    fun `getVenues() returns null when error code is other than OK`() {
        // Arrange
        val latLng = "10.1522,11.2545"
        val queryWord = "coffee"
        every {
            api.getVenuesByLocation(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns getMockVenueResponse(404)

        // Act
        val result = runBlocking {
            presenter.getVenues(queryWord, latLng)
        }

        // Assert
        assertNull(result)
        coVerify(exactly = 1) {
            api.getVenuesByLocation(any(), any(), any(), latLng, queryWord, any())
        }
        confirmVerified(api)
    }

    @Test
    fun `searchForVenues() empties the venue list and launches VenueLoaderJob when called`() {
        // Arrange
        val queryWord = "Cafe"

        // Act
        runBlocking {
            presenter.searchForVenues(queryWord)
        }

        // Assert
        verify {
            venueSearchView.updateVenueList(any())
            venueSearchView.updateInfoTextVisibility(any())
            venueSearchView.updateLoadingProgressBarVisibility(any())
        }
        coVerify {
            currentLocationProvider.startLocationUpdates(any())
            currentLocationProvider.getCurrentLocation()
            api.getVenuesByLocation(any(), any(), any(), any(), queryWord, any())
            prefs.setLatestLatLng(any())
        }
        confirmVerified(venueSearchView, currentLocationProvider, api, prefs)
    }

    @Test
    fun `launchVenueLoaderJob updates the venue list with empty list when location is not received`() {
        // Arrange
        every { prefs.getLatestLatLng() } returns null
        every { currentLocationProvider.getCurrentLocation() } returns null
        val queryWord = "Cafe"

        // Act
        runBlocking {
            presenter.launchVenueLoaderJob(queryWord)
        }

        // Assert
        coVerify(exactly = 1) {
            currentLocationProvider.startLocationUpdates(any())
            currentLocationProvider.getCurrentLocation()
            prefs.getLatestLatLng()
            venueSearchView.updateVenueList(emptyList())
        }
        confirmVerified(prefs, venueSearchView, currentLocationProvider)
    }

    @Test
    fun `launchVenueLoaderJob fetches venues from FourSquare API when location is available`() {
        // Arrange
        val queryWord = "Cafe"

        // Act
        runBlocking {
            presenter.launchVenueLoaderJob(queryWord)
        }

        // Assert
        coVerify {
            currentLocationProvider.startLocationUpdates(any())
            currentLocationProvider.getCurrentLocation()
            api.getVenuesByLocation(any(), any(), any(), any(), queryWord, any())
            prefs.setLatestLatLng(any())
            venueSearchView.updateLoadingProgressBarVisibility(any())
            venueSearchView.updateVenueList(any())
        }
        confirmVerified(api, prefs, venueSearchView, currentLocationProvider)
    }

    private fun getMockVenueResponse(resultCode: Int): Call<VenueResponse> {
        return object : Call<VenueResponse> {
            override fun enqueue(callback: Callback<VenueResponse>?) {
            }

            override fun isExecuted(): Boolean {
                return false
            }

            override fun clone(): Call<VenueResponse> {
                return this
            }

            override fun isCanceled(): Boolean {
                return false
            }

            override fun cancel() {

            }

            override fun request(): Request {
                return Request.Builder().build()
            }

            override fun execute(): retrofit2.Response<VenueResponse> {
                val meta = Meta(1, "1")
                val location = VenueLocation("Imaginary land 1", 2000)
                val items = listOf(
                    VenueItem("1", "Cocoa shop", location),
                    VenueItem("2", "Coffee shop", location),
                    VenueItem("3", "Tea shop", location)
                )
                val response = Response(items)
                val venueResponse = VenueResponse(meta, response)
                return if (resultCode == CODE_OK) {
                    retrofit2.Response.success(venueResponse)
                } else {
                    retrofit2.Response.error(
                        resultCode,
                        ResponseBody.create(MediaType.parse("test"), "")
                    )
                }
            }

            override fun timeout(): Timeout {
                return Timeout()
            }
        }
    }
}