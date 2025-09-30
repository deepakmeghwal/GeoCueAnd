package com.geocue.android.ui.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocue.android.domain.GeofenceInteractor
import com.geocue.android.domain.model.GeofenceLocation
import com.geocue.android.location.AndroidLocationClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val interactor: GeofenceInteractor,
    private val locationClient: AndroidLocationClient
) : ViewModel() {

    private val _cameraLocation = MutableStateFlow<Location?>(null)
    val cameraLocation: StateFlow<Location?> = _cameraLocation

    val uiState: StateFlow<MapUiState> = interactor.observe()
        .combine(_cameraLocation) { geofences, location ->
            MapUiState(geofences = geofences, userLocation = location)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState()
        )

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _cameraLocation.value = locationClient.getCurrentLocation()
        }
    }
}

data class MapUiState(
    val geofences: List<GeofenceLocation> = emptyList(),
    val userLocation: Location? = null
)
