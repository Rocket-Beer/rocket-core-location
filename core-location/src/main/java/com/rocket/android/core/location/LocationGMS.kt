package com.rocket.android.core.location

import android.Manifest
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.rocket.android.core.data.location.error.LocationFailure
import com.rocket.android.core.data.permissions.Permissions
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class LocationGMS(
    permissions: Permissions,
    private val locationClient: FusedLocationProviderClient,
    private val looper: Looper
) : CoreDataLocation(permissions = permissions) {

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = intervalMilliseconds
            fastestInterval = fastestInterval
            priority = when (locationPriority) {
                Priority.PRIORITY_HIGH_ACCURACY -> LocationRequest.PRIORITY_HIGH_ACCURACY
                Priority.PRIORITY_BALANCED_POWER_ACCURACY -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }
        }
    }

    @VisibleForTesting
    internal fun getLocationRequestForTest(): LocationRequest = locationRequest

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            if (result.lastLocation == null) {
                _locationFlow.value = LocationFailure.NoData.left()
            } else {
                _locationFlow.value = result.lastLocation.right()
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability?) {
            super.onLocationAvailability(availability)

            if (availability == null || !availability.isLocationAvailable) {
                _locationFlow.value = LocationFailure.NoData.left()
            }
        }
    }

    @VisibleForTesting
    internal fun getLocationCallbackForTest(): LocationCallback = locationCallback

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun startLocation() {
        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            looper
        )
    }

    override fun stopLocation() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    override suspend fun getLastLocation(): Either<LocationFailure, Location> =
        suspendCancellableCoroutine { continuation ->
            locationClient.lastLocation
                .addOnCompleteListener { }
                .addOnFailureListener { exception ->
                    continuation.resume(
                        value = LocationFailure.Error(msg = exception.message).left()
                    )
                }
                .addOnSuccessListener { location ->
                    if (location == null) {
                        continuation.resume(value = LocationFailure.NoData.left())
                    } else {
                        continuation.resume(value = location.right())
                    }
                }
                .addOnCanceledListener {
                    continuation.resume(value = LocationFailure.Cancelled.left())
                }
        }

}