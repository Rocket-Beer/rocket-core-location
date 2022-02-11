package com.rocket.android.core.data.location

import android.location.Location
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rocket.android.core.data.location.error.LocationFailure
import com.rocket.android.core.data.permissions.Permissions
import com.huawei.hmf.tasks.Tasks
import com.huawei.hms.location.FusedLocationProviderClient
import com.huawei.hms.location.HWLocation
import com.huawei.hms.location.LocationResult
import com.karumi.dexter.Dexter
import io.mockk.every
import io.mockk.mockkClass
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationHMSTest : TestCase() {

    private val looper = Looper.getMainLooper()

    private val locationClient: FusedLocationProviderClient =
        mockkClass(FusedLocationProviderClient::class)

    private val locationHMS = LocationHMS(
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

        every {
            locationClient.lastLocation
        } answers {
            Tasks.fromResult(Location("mockProvider").apply {
                latitude = myLatitude
                longitude = myLongitude
                accuracy = myAccuracy
            })
        }

        locationHMS.getLastLocation().fold(
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
        every {
            locationClient.lastLocation
        } answers {
            Tasks.fromResult(null)
        }

        locationHMS.getLastLocation().fold(
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
        every {
            locationClient.lastLocation
        } answers {
            Tasks.fromCanceled()
        }

        locationHMS.getLastLocation().fold(
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
        every {
            locationClient.lastLocation
        } answers {
            Tasks.fromException(Exception("Error"))
        }

        locationHMS.getLastLocation().fold(
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
        val callback = locationHMS.getLocationCallbackForTest()

        every {
            locationClient.requestLocationUpdates(
                locationHMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationAvailability(null)
            Tasks.fromResult(null)
        }

        locationHMS.startLocation()

        locationHMS.locationFlow.first().fold(
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
        val callback = locationHMS.getLocationCallbackForTest()

        every {
            locationClient.requestLocationUpdates(
                locationHMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationResult(LocationResult.create(listOf()))
            Tasks.fromResult(null)
        }

        locationHMS.startLocation()

        locationHMS.locationFlow.first().fold(
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
        val callback = locationHMS.getLocationCallbackForTest()
        val provider = "mockLocation"

        every {
            locationClient.requestLocationUpdates(
                locationHMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationResult(LocationResult.create(listOf(HWLocation().apply {
                setProvider(
                    provider
                )
            })))
            Tasks.fromResult(null)
        }

        locationHMS.startLocation()

        locationHMS.locationFlow.first().fold(
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
        val callback = locationHMS.getLocationCallbackForTest()
        val provider1 = "mockLocation1"
        val provider2 = "mockLocation2"

        every {
            locationClient.requestLocationUpdates(
                locationHMS.getLocationRequestForTest(),
                callback,
                looper
            )
        } answers {
            callback.onLocationResult(
                LocationResult.create(
                    listOf(
                        HWLocation().apply { provider = provider1 },
                        HWLocation().apply { provider = provider2 }
                    )
                )
            )
            Tasks.fromResult(null)
        }

        locationHMS.startLocation()

        locationHMS.locationFlow.value.fold(
            {
                fail()
            },
            { location ->
                assertEquals(provider2, location.provider)
            }
        )

    }

}