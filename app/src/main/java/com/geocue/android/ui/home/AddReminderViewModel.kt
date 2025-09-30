package com.geocue.android.ui.home

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geocue.android.location.AndroidLocationClient
import com.geocue.android.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationClient: AndroidLocationClient,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val geocoder: Geocoder? = if (Geocoder.isPresent()) {
        Geocoder(context, Locale.getDefault())
    } else {
        null
    }

    private val _state = MutableStateFlow(AddReminderUiState())
    val state: StateFlow<AddReminderUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        if (geocoder == null) {
            _state.value = _state.value.copy(searchError = "Geocoding services are unavailable on this device.")
        }
    }

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()

        if (geocoder == null) {
            return
        }

        if (query.length < MIN_QUERY_LENGTH) {
            _state.value = _state.value.copy(results = emptyList(), isSearching = false, searchError = null)
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        _state.value = _state.value.copy(isSearching = true, searchError = null)

        val addressesResult = withContext(Dispatchers.IO) {
            runCatching { geocoder?.getFromLocationName(query, MAX_RESULTS).orEmpty() }
        }

        val current = _state.value

        if (current.query != query) {
            return
        }

        if (addressesResult.isSuccess) {
            val results = addressesResult.getOrNull().orEmpty().mapNotNull { it.toResult() }
            val selection = current.selectedLocation ?: results.firstOrNull()
            _state.value = current.copy(
                isSearching = false,
                results = results,
                selectedLocation = selection,
                name = if (current.name.isBlank()) selection?.name ?: "" else current.name,
                searchError = null
            )
        } else {
            _state.value = current.copy(
                isSearching = false,
                results = emptyList(),
                searchError = "Unable to search for that location."
            )
        }
    }

    fun selectResult(result: PlaceSearchResult) {
        val currentName = _state.value.name
        _state.value = _state.value.copy(
            selectedLocation = result,
            name = if (currentName.isBlank()) result.name else currentName,
            query = result.name
        )
    }

    fun updateRadius(radius: Float) {
        _state.value = _state.value.copy(radius = radius)
    }

    fun updateEntryMessage(message: String) {
        _state.value = _state.value.copy(entryMessage = message)
    }

    fun updateExitMessage(message: String) {
        _state.value = _state.value.copy(exitMessage = message)
    }

    fun toggleNotifyOnEntry(checked: Boolean) {
        _state.value = _state.value.copy(notifyOnEntry = checked)
    }

    fun toggleNotifyOnExit(checked: Boolean) {
        _state.value = _state.value.copy(notifyOnExit = checked)
    }

    fun prepare() {
        viewModelScope.launch {
            if (!permissionChecker.hasLocationPermission()) return@launch
            val location = runCatching { locationClient.getCurrentLocation() }.getOrNull()
            if (location != null) {
                _state.value = _state.value.copy(currentLocation = LocationPoint(location.latitude, location.longitude))
            }
        }
    }

    fun buildRequest(): CreateGeofenceRequest? {
        val selected = _state.value.selectedLocation ?: return null
        val trimmedName = _state.value.name.trim()
        if (trimmedName.isEmpty()) return null
        return CreateGeofenceRequest(
            name = trimmedName,
            address = selected.address ?: "",
            latitude = selected.latitude,
            longitude = selected.longitude,
            radius = _state.value.radius,
            entryMessage = _state.value.entryMessage,
            exitMessage = _state.value.exitMessage,
            notifyOnEntry = _state.value.notifyOnEntry,
            notifyOnExit = _state.value.notifyOnExit
        )
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun reset() {
        _state.value = AddReminderUiState(currentLocation = _state.value.currentLocation)
    }

    fun selectCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val resolved = if (geocoder != null) {
                withContext(Dispatchers.IO) {
                    runCatching { geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.toResult() }
                        .getOrNull()
                }
            } else {
                null
            }

            val result = resolved ?: PlaceSearchResult(
                name = "Pinned location",
                address = null,
                latitude = latitude,
                longitude = longitude
            )

            selectResult(result)
        }
    }

    fun useCurrentLocation() {
        val current = _state.value.currentLocation ?: return
        selectCoordinates(current.latitude, current.longitude)
    }

    private fun Address.toResult(): PlaceSearchResult? {
        val latValue = this.latitude
        val lngValue = this.longitude
        if (!latValue.isFinite() || !lngValue.isFinite()) return null
        val primary = featureName ?: thoroughfare ?: subLocality ?: locality ?: adminArea ?: countryName ?: return null
        val formattedAddress = listOfNotNull(thoroughfare, locality, adminArea, countryName)
            .joinToString(separator = ", ")
            .ifBlank { null }
        return PlaceSearchResult(
            name = primary,
            address = formattedAddress,
            latitude = latValue,
            longitude = lngValue
        )
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 3
        private const val MAX_RESULTS = 5
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}

data class AddReminderUiState(
    val query: String = "",
    val name: String = "",
    val results: List<PlaceSearchResult> = emptyList(),
    val selectedLocation: PlaceSearchResult? = null,
    val radius: Float = 150f,
    val entryMessage: String = "",
    val exitMessage: String = "",
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = false,
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val currentLocation: LocationPoint? = null
)

data class PlaceSearchResult(
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double
)

data class LocationPoint(val latitude: Double, val longitude: Double)
