package com.rocket.android.core.location.gms.di

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.rocket.android.core.data.permissions.Permissions
import com.rocket.android.core.data.permissions.di.CoreDataProvider
import com.rocket.android.core.location.gms.CoreDataLocationGMS
import com.rocket.android.core.location.gms.LocationGMS

@Suppress("unused")
class CoreDataLocationProviderGMS(
    private val context: Context,
    private val permissions: Permissions = CoreDataProvider(context = context).provideCorePermissions,
    private val looper: Looper = Looper.getMainLooper()

) {

    val provideCoreLocation: CoreDataLocationGMS by lazy {

        LocationGMS(
            permissions = permissions,
            locationClient = LocationServices.getFusedLocationProviderClient(context),
            looper = Looper.getMainLooper()
        )
    }

    companion object {

        @Suppress("ObjectPropertyName")
        private lateinit var _instance: CoreDataLocationProviderGMS

        fun getInstance(
            context: Context,
            permissions: Permissions,
            looper: Looper
        ): CoreDataLocationProviderGMS =
            synchronized(this) {
                if (Companion::_instance.isInitialized) {
                    _instance
                } else {
                    _instance =
                        CoreDataLocationProviderGMS(
                            context = context,
                            permissions = permissions,
                            looper = looper
                        )
                    _instance
                }
            }
    }
}
