package com.geocue.android.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AndroidLocationClient(private val app: Application) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(intervalMillis: Long = 10_000L): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(intervalMillis)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it).isSuccess }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, app.mainLooper)

        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = fusedClient.lastLocation.await()
}
