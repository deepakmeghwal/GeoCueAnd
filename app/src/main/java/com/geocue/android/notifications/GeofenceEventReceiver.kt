package com.geocue.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geocue.android.data.GeofenceRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GeofenceEventReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: GeofenceRepository
    @Inject lateinit var notificationManager: GeofenceNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val event = GeofencingEvent.fromIntent(intent) ?: return@runCatching
                if (event.hasError()) return@runCatching
                val transition = when (event.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> GeofenceTransition.ENTRY
                    Geofence.GEOFENCE_TRANSITION_EXIT -> GeofenceTransition.EXIT
                    else -> return@runCatching
                }

                val geofences = repository.getGeofences().associateBy { it.id.toString() }
                event.triggeringGeofences?.forEach { geofence ->
                    geofences[geofence.requestId]?.let { location ->
                        // Only send notification if the location is enabled
                        if (!location.isEnabled) return@let
                        
                        // Check if we should notify based on transition type
                        val shouldNotify = when (transition) {
                            GeofenceTransition.ENTRY -> location.notifyOnEntry
                            GeofenceTransition.EXIT -> location.notifyOnExit
                        }
                        
                        if (shouldNotify) {
                            notificationManager.notifyEvent(location, transition)
                        }
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
