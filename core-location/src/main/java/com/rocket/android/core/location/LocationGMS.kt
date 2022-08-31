package com.rocket.android.core.location

import android.Manifest
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.rocket.android.core.data.permissions.Permissions
import com.rocket.android.core.location.error.LocationFailure
import com.rocket.core.domain.functional.Either
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
                _locationFlow.value = Either.Left(LocationFailure.NoData)
            } else {
                _locationFlow.value = Either.Right(result.lastLocation)
            }
        }
        override fun onLocationAvailability(availability: LocationAvailability) {
            super.onLocationAvailability(availability)

            if (!availability.isLocationAvailable) {
                _locationFlow.value = Either.Left(LocationFailure.NoData)
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
                        value = Either.Left(
                            LocationFailure.Error(msg = exception.message)
                        )
                    )
                }
                .addOnSuccessListener { location ->
                    if (location == null) {
                        continuation.resume(value = Either.Left(LocationFailure.NoData))
                    } else {
                        continuation.resume(value = Either.Right(location))
                    }
                }
                .addOnCanceledListener {
                    continuation.resume(value = Either.Left(LocationFailure.Cancelled))
                }
        }
}
