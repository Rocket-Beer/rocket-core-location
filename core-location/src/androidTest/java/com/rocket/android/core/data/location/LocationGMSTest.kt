package com.rocket.android.core.data.location

import android.location.Location
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rocket.android.core.data.location.error.LocationFailure
import com.rocket.android.core.data.permissions.Permissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Tasks
import com.karumi.dexter.Dexter
import io.mockk.every
import io.mockk.mockkClass
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationGMSTest : TestCase() {

    private val looper = Looper.getMainLooper()
    private val locationClient: FusedLocationProviderClient =
        mockkClass(FusedLocationProviderClient::class)

    private val locationGMS = LocationGMS(
        permissions = Permissions(
            dexter = Dexter.withContext(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
        ),
        looper = Looper.getMainLooper(),
        locationClient = locationClient
    )

    @Test
    fun givenRightFakeLocation_whenGetLastLocation_returnsLocation() = runBlocking {
        val myLatitude = 40.4530406
        val myLongitude = -3.6905596
        val myAccuracy = 30f

        every { locationClient.lastLocation } answers  {
            Tasks.forResult(Location("mockProvider").apply {
                latitude = myLatitude
                longitude = myLongitude
                accuracy = myAccuracy
            })
        }

        locationGMS.getLastLocation().fold(
            {
                fail()
            },
            { location ->
                assertEquals(myLatitude, location.latitude)
                assertEquals(myLongitude, location.longitude)
                assertEquals(myAccuracy, location.accuracy)
            }
        )
    }

    @Test
    fun givenNullFakeLocation_whenGetLastLocation_returnsNoData() = runBlocking {
        every { locationClient.lastLocation } answers {
            Tasks.forResult(null)
        }

        locationGMS.getLastLocation().fold(
            { failure ->
                assertTrue(failure is LocationFailure.NoData)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenCanceledLocation_whenGetLastLocation_returnsCancelFailure() = runBlocking {
        every { locationClient.lastLocation } answers {
            Tasks.forCanceled()
        }

        locationGMS.getLastLocation().fold(
            { failure ->
                assertTrue(failure is LocationFailure.Cancelled)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenFailureLocation_whenGetLastLocation_returnsFailure() = runBlocking {
        every { locationClient.lastLocation } answers {
            Tasks.forException(Exception("Error"))
        }

        locationGMS.getLastLocation().fold(
            { failure ->
                assertTrue(failure is LocationFailure.Error)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenNotAvailableLocation_whenStartLocation_noDataFailureReceived() = runBlocking {
        val callback = locationGMS.getLocationCallbackForTest()

        every {
            locationClient.requestLocationUpdates(
                locationGMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationAvailability(null)
            Tasks.forResult(null)
        }

        locationGMS.startLocation()

        locationGMS.locationFlow.first().fold(
            { failure ->
                assertTrue(failure is LocationFailure.NoData)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenEmptyLocations_whenStartLocation_noDataFailureReceived() = runBlocking {
        val callback = locationGMS.getLocationCallbackForTest()

        every {
            locationClient.requestLocationUpdates(
                locationGMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationResult(LocationResult.create(listOf()))
            Tasks.forResult(null)
        }

        locationGMS.startLocation()

        locationGMS.locationFlow.first().fold(
            { failure ->
                assertTrue(failure is LocationFailure.NoData)
            },
            {
                fail()
            }
        )
    }

    @Test
    fun givenLocation_whenStartLocation_locationIsReceived() = runBlocking {
        val callback = locationGMS.getLocationCallbackForTest()
        val provider = "mockLocation"

        every {
            locationClient.requestLocationUpdates(
                locationGMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationResult(LocationResult.create(listOf(Location(provider))))
            Tasks.forResult(null)
        }

        locationGMS.startLocation()

        locationGMS.locationFlow.first().fold(
            {
                fail()
            },
            { location ->
                assertEquals(provider, location.provider)
            }
        )
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    @Test
    fun givenLocations_whenStartLocation_latestLocationIsReceived() = runBlocking {
        val callback = locationGMS.getLocationCallbackForTest()
        val provider1 = "mockLocation1"
        val provider2 = "mockLocation2"

        every {
            locationClient.requestLocationUpdates(
                locationGMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationResult(
                LocationResult.create(
                    listOf(
                        Location(provider1),
                        Location(provider2)
                    )
                )
            )
            Tasks.forResult(null)
        }

        locationGMS.startLocation()

        locationGMS.locationFlow.value.fold(
            {
                fail()
            },
            { location ->
                assertEquals(provider2, location.provider)
            }
        )
    }

}