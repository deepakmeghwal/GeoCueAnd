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
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

@AndroidEntryPoint
class GeofenceEventReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: GeofenceRepository
    @Inject lateinit var notificationManager: GeofenceNotificationManager

    companion object {
        // Track locations where we've delivered a DWELL notification to allow EXIT only after dwell
        private val dwelledLocationIds: MutableSet<String> =
            Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val event = GeofencingEvent.fromIntent(intent) ?: return@runCatching
                if (event.hasError()) return@runCatching
                val geofences = repository.getGeofences().associateBy { it.id.toString() }
                event.triggeringGeofences?.forEach { geofence ->
                    geofences[geofence.requestId]?.let { location ->
                        // Only send notification if the location is enabled
                        if (!location.isEnabled) return@let

                        when (event.geofenceTransition) {
                            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                                if (location.notifyOnEntry) {
                                    // Treat DWELL as a matured ENTRY and notify
                                    notificationManager.notifyEvent(location, GeofenceTransition.ENTRY)
                                }
                                dwelledLocationIds.add(location.id.toString())
                            }
                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                // Only notify EXIT if we previously dwelled inside this location
                                val hadDwelled = dwelledLocationIds.remove(location.id.toString())
                                if (hadDwelled && location.notifyOnExit) {
                                    notificationManager.notifyEvent(location, GeofenceTransition.EXIT)
                                }
                            }
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                // Ignore raw ENTER to avoid drive-by notifications
                            }
                            else -> {
                                // Ignore other transitions
                            }
                        }
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
