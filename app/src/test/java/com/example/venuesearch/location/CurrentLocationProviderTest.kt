package com.example.venuesearch.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class CurrentLocationProviderTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var locationManager: LocationManager

    @MockK
    private lateinit var mockLocation: Location

    @MockK
    private lateinit var mockLocationUpdateReceivedListener : LocationUpdateReceivedListener

    private lateinit var currentLocationProvider: CurrentLocationProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(ContextCompat::class)
        mockkStatic(LocationManager::class)
        currentLocationProvider = CurrentLocationProvider(context, locationManager)
        every { mockLocationUpdateReceivedListener.onLocationUpdateReceived() } returns Unit
        every { mockLocationUpdateReceivedListener.onLocationPermissionRequested() } returns Unit
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Current location is returned when location update has been received`() {
        // Arrange
        every { ActivityCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MINIMUM_LOCATION_UPDATE_INTERVAL_MS,
            MINIMUM_LOCATION_UPDATE_DISTANCE_METERS,
            currentLocationProvider) } answers { currentLocationProvider.onLocationChanged(mockLocation) }

        // Act
        currentLocationProvider.startLocationUpdates(mockLocationUpdateReceivedListener)
        val result = currentLocationProvider.getCurrentLocation()

        // Assert
        assertEquals(mockLocation, result)
    }

    @Test
    fun `Fine location permission is requested when permissions are not granted when location updates are started`() {
        // Arrange
        every { ActivityCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MINIMUM_LOCATION_UPDATE_INTERVAL_MS,
            MINIMUM_LOCATION_UPDATE_DISTANCE_METERS,
            currentLocationProvider) } answers { currentLocationProvider.onLocationChanged(mockLocation) }

        // Act
        currentLocationProvider.startLocationUpdates(mockLocationUpdateReceivedListener)

        // Assert
        verify(exactly = 1) {
            mockLocationUpdateReceivedListener.onLocationPermissionRequested()
        }
        confirmVerified(mockLocationUpdateReceivedListener)
    }

    @Test
    fun `Current location is null when location updates are not started`() {
        // Arrange
        every { ActivityCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // Act
        val result = currentLocationProvider.getCurrentLocation()

        // Assert
        assertNull(result)
    }

    @Test
    fun `Location is available when fine & coarse location permissions are granted`() {
        // Arrange
        every { ActivityCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // Act
        val result = currentLocationProvider.isLocationAvailable()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `Location is not available when fine location permission is not granted`() {
        // Arrange
        every { ActivityCompat.checkSelfPermission(any(), ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { ActivityCompat.checkSelfPermission(any(), ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED

        // Act
        val result = currentLocationProvider.isLocationAvailable()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `Location is not available when coarse location permission is not granted`() {
        // Arrange
        every { ActivityCompat.checkSelfPermission(any(), ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { ActivityCompat.checkSelfPermission(any(), ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        // Act
        val result = currentLocationProvider.isLocationAvailable()

        // Assert
        assertFalse(result)
    }
}