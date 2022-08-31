package com.rocket.android.core.location

import android.Manifest
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import com.huawei.hms.location.FusedLocationProviderClient
import com.huawei.hms.location.LocationAvailability
import com.huawei.hms.location.LocationCallback
import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationResult
import com.rocket.android.core.data.permissions.Permissions
import com.rocket.android.core.location.error.LocationFailure
import com.rocket.core.domain.functional.Either
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHMS(
    permissions: Permissions,
    private val locationClient: FusedLocationProviderClient,
    private val looper: Looper
) : CoreDataLocation(permissions = permissions) {
    private val locationRequest by lazy {
        LocationRequest.create().apply {
            interval = intervalMilliseconds
            fastestInterval = fastestIntervalMilliseconds
            priority = when (locationPriority) {
                Priority.PRIORITY_HIGH_ACCURACY -> LocationRequest.PRIORITY_HIGH_ACCURACY
                Priority.PRIORITY_BALANCED_POWER_ACCURACY -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }
        }
    }

    @VisibleForTesting
    fun getLocationRequestForTest(): LocationRequest = locationRequest

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

        override fun onLocationAvailability(availability: LocationAvailability?) {
            super.onLocationAvailability(availability)

            if (availability == null || !availability.isLocationAvailable)
                _locationFlow.value = Either.Left(LocationFailure.NoData)
        }
    }

    @VisibleForTesting
    fun getLocationCallbackForTest(): LocationCallback = locationCallback

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
    override suspend fun getLastLocation(): Either<LocationFailure, Location> = suspendCancellableCoroutine { continuation ->
        locationClient.lastLocation
            .addOnCompleteListener { }
            .addOnFailureListener { exception ->
                continuation.resume(value = Either.Left(LocationFailure.Error(msg = exception.message)))
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