package com.geocue.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocue.android.data.GeofenceRepository
import com.geocue.android.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val geofenceRepository: GeofenceRepository
) : ViewModel() {

    private val _permissionState = MutableStateFlow(permissionSnapshot())

    val uiState: StateFlow<SettingsUiState> = geofenceRepository.observeGeofences()
        .combine(_permissionState) { geofences, permissions ->
            SettingsUiState(
                totalReminders = geofences.size,
                hasForegroundPermission = permissions.hasForeground,
                hasBackgroundPermission = permissions.hasBackground,
                hasNotificationPermission = permissions.hasNotifications
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun refreshPermissions() {
        viewModelScope.launch {
            _permissionState.value = permissionSnapshot()
        }
    }

    private fun permissionSnapshot(): PermissionSnapshot = PermissionSnapshot(
        hasForeground = permissionChecker.hasLocationPermission(),
        hasBackground = permissionChecker.hasBackgroundLocationPermission(),
        hasNotifications = permissionChecker.hasNotificationsPermission()
    )
}

data class SettingsUiState(
    val totalReminders: Int = 0,
    val hasForegroundPermission: Boolean = false,
    val hasBackgroundPermission: Boolean = false,
    val hasNotificationPermission: Boolean = true
)

data class PermissionSnapshot(
    val hasForeground: Boolean,
    val hasBackground: Boolean,
    val hasNotifications: Boolean
)
