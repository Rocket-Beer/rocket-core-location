package com.rocket.android.core.data.location

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rocket.android.core.data.di.CoreDataProvider
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionsIntegrationTest : TestCase() {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val permissions = CoreDataProvider.getInstance(context = context).provideCorePermissions

    @Test
    fun givenNotDefinedPermission_whenCheckSinglePermission_deniedResultReceived() = runBlocking {
        val response = permissions.checkSinglePermission(permission = Manifest.permission.CAMERA)
        assertTrue(response.isLeft())
    }

    @Test
    fun givenDefinedPermission_whenCheckSinglePermission_grantedResultReceived() = runBlocking {
        val response =
            permissions.checkSinglePermission(permission = Manifest.permission.ACCESS_COARSE_LOCATION)
        assertTrue(response.isRight())
    }

    @Test
    fun givenNotDefinedPermissions_whenCheckMultiplePermissions_deniedResultReceived() =
        runBlocking {
            val response = permissions.checkMultiplePermissions(
                permissions = listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            assertTrue(response.isLeft())
        }

    @Test
    fun givenDefinedPermissions_whenCheckMultiplePermissions_grantedResultReceived() = runBlocking {
        val response = permissions.checkMultiplePermissions(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        assertTrue(response.isRight())
    }
}