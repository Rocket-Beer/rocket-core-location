package com.rocket.android.core.location.sample.app

import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.rocket.android.core.location.gms.CoreDataLocationGMS
import com.rocket.android.core.location.gms.di.CoreDataLocationProviderGMS


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val location: CoreDataLocationGMS by lazy {
        CoreDataLocationProviderGMS(context = this).provideCoreLocation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Location()

    }

    @SuppressLint("MissingPermission")
    fun Location() {

        GlobalScope.launch(Dispatchers.Main) {
            location.checkPermissions().fold({ error ->
                showToast()
                Log.d("Rocket APP", "Rocket APP :: Error happened with permissions - data =")
            }, {
                Log.d("Rocket APP", "Rocket APP :: Permissions success")
                location.startLocation()
                location.getLastLocation().fold({ locationFailure ->
                    showToast()
                }, { location ->
                    showtoastcorret(location)
                })
            })
        }
    }

    private fun checkP() {
        Location()
    }


    fun showToast() {
        Toast.makeText(this, "Necesitamos el permiso para que la app funcione", Toast.LENGTH_LONG).show()
    }

    fun showtoastcorret(location: Location) {
        Toast.makeText(
            this,
            "Location ${location.provider} : (${location.latitude} ,${location.longitude})",
            Toast.LENGTH_LONG
        ).show()
    }
}