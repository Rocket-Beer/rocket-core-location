package com.rocket.android.core.location.gms

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import androidx.annotation.RequiresPermission
import com.rocket.android.core.data.permissions.PermissionResponse
import com.rocket.android.core.data.permissions.Permissions
import com.rocket.android.core.location.gms.error.LocationFailureGMS
import com.rocket.core.domain.functional.Either
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class CoreDataLocationGMS(
    private val permissions: Permissions
) {

    enum class Priority {
        PRIORITY_HIGH_ACCURACY,
        PRIORITY_BALANCED_POWER_ACCURACY
    }

    protected var intervalMilliseconds: Long = INTERVAL_MILLISECONDS
    protected var fastestIntervalMilliseconds: Long = FASTEST_INTERVAL_MILLISECONDS
    protected var locationPriority: Priority = Priority.PRIORITY_HIGH_ACCURACY

    @Suppress("PropertyName", "EXPERIMENTAL_API_USAGE")
    protected val _gmsLocation =
        MutableStateFlow<Either<LocationFailureGMS, Location>>(value = Either.Left(LocationFailureGMS.NoData))

    @Suppress("EXPERIMENTAL_API_USAGE")
    val gmsLocation: StateFlow<Either<LocationFailureGMS, Location>>
        get() = _gmsLocation

    @Suppress("unused")
    fun setConfiguration(
        intervalMilliseconds: Long = INTERVAL_MILLISECONDS,
        fastestIntervalMilliseconds: Long = FASTEST_INTERVAL_MILLISECONDS,
        priority: Priority = Priority.PRIORITY_HIGH_ACCURACY
    ) {
        this.intervalMilliseconds = intervalMilliseconds
        this.fastestIntervalMilliseconds = fastestIntervalMilliseconds
        locationPriority = priority
    }

    suspend fun checkPermissions():
        Either<PermissionResponse.MultiplePermissionDenied, PermissionResponse.PermissionGranted> =
        permissions.checkMultiplePermissions(
            permissions = listOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
            )
        )

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    abstract fun startLocation()

    abstract fun stopLocation()

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    abstract suspend fun getLastLocation(): Either<LocationFailureGMS, Location>

    companion object {
        private const val INTERVAL_MILLISECONDS = 60000L
        private const val FASTEST_INTERVAL_MILLISECONDS = 10000L
    }
}
