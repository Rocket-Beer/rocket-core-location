package com.rocket.android.core.location.di

import android.content.Context
import android.os.Looper
import com.rocket.android.core.data.location.CoreDataLocation
import com.rocket.android.core.data.location.LocationGMS
import com.rocket.android.core.data.location.LocationHMS
import com.rocket.android.core.data.permissions.Permissions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.huawei.hms.api.HuaweiApiAvailability

@Suppress("unused")
class CoreDataLocationProvider private constructor(
    private val context: Context,
    private val permissions: Permissions,
    private val looper: Looper
) {

    val provideCoreLocation: CoreDataLocation by lazy {
        when {
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS -> {
                LocationGMS(
                    permissions = permissions,
                    locationClient = LocationServices.getFusedLocationProviderClient(context),
                    looper = Looper.getMainLooper()
                )
            }
            HuaweiApiAvailability.getInstance()
                .isHuaweiMobileServicesAvailable(context) == com.huawei.hms.api.ConnectionResult.SUCCESS -> {
                LocationHMS(
                    permissions = permissions,
                    locationClient = com.huawei.hms.location.LocationServices.getFusedLocationProviderClient(
                        context
                    ),
                    looper = Looper.getMainLooper()
                )
            }
            else -> {
                LocationGMS(
                    permissions = permissions,
                    locationClient = LocationServices.getFusedLocationProviderClient(context),
                    looper = Looper.getMainLooper()
                )
            }
        }
    }

    companion object {

        @Suppress("ObjectPropertyName")
        private lateinit var _instance: CoreDataLocationProvider

        fun getInstance(
            context: Context,
            permissions: Permissions,
            looper: Looper
        ): CoreDataLocationProvider =
            synchronized(this) {
                if (::_instance.isInitialized) {
                    _instance
                } else {
                    _instance =
                        CoreDataLocationProvider(
                            context = context,
                            permissions = permissions,
                            looper = looper
                        )
                    _instance
                }
            }

    }

}