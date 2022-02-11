package com.rocket.android.core.data.location

import android.Manifest
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.left
import arrow.core.right
import com.rocket.android.core.data.permissions.PermissionResponse
import com.rocket.android.core.data.permissions.Permissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionRequest
import io.mockk.coEvery
import io.mockk.mockkClass
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreLocationTest : TestCase() {

    private val permissions = mockkClass(Permissions::class)
    private val locationClient = mockkClass(FusedLocationProviderClient::class)
    private val looper: Looper = Looper.getMainLooper()

    private val locationHandler: CoreDataLocation =
        LocationGMS(permissions = permissions, locationClient = locationClient, looper = looper)

    @Test
    fun givenDeniedPermission_whenGetLastLocation_permissionDeniedFailureResponse() = runBlocking {
        coEvery {
            permissions.checkMultiplePermissions(
                permissions = listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } answers {
            PermissionResponse.MultiplePermissionDenied(
                data = listOf(
                    PermissionDeniedResponse(
                        PermissionRequest(""),
                        false
                    )
                )
            ).left()
        }

        val response = locationHandler.checkPermissions()

        assertTrue(response.isLeft())
    }

    @Test
    fun givenGrantedPermission_whenGetLastLocation_lastLocationResponse() = runBlocking {
        coEvery {
            permissions.checkMultiplePermissions(
                permissions = listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } answers {
            PermissionResponse.PermissionGranted.right()
        }

        val response = locationHandler.checkPermissions()
        assertTrue(response.isRight())
    }

}

