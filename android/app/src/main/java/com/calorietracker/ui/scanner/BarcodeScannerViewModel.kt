package com.calorietracker.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.food.FoodBarcodeResult
import com.calorietracker.data.food.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where the scanner should send the user once a barcode is resolved. */
sealed interface ScanNavigation {

    /** The barcode matched a known food; open its detail screen. */
    data class ToFoodDetail(val foodId: Long) : ScanNavigation

    /** The barcode is unknown; open the custom-food form pre-filled with it. */
    data class ToCustomFood(val barcode: String) : ScanNavigation
}

/**
 * UI state for the barcode scanner.
 *
 * @property isResolving a lookup is in flight; further detections are ignored.
 * @property navigation the resolved destination, consumed once by the screen.
 * @property errorMessage a user-facing lookup error, or `null`.
 */
data class BarcodeScannerUiState(
    val isResolving: Boolean = false,
    val navigation: ScanNavigation? = null,
    val errorMessage: String? = null,
)

/**
 * Drives the barcode scanner: turns a decoded EAN into a single backend lookup and a
 * navigation decision. The camera analyzer reports the same code on many frames, so
 * detections are de-duplicated while a lookup is in flight or once one has resolved.
 */
@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val repository: FoodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeScannerUiState())
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    /**
     * Handles a barcode decoded from the camera. Ignored if a lookup is already running
     * or a destination has already been resolved, so a single scan triggers one lookup.
     */
    fun onBarcodeDetected(ean: String) {
        val state = _uiState.value
        if (state.isResolving || state.navigation != null) {
            return
        }

        _uiState.update { it.copy(isResolving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.foodByBarcode(ean)) {
                is FoodBarcodeResult.Found ->
                    _uiState.update { it.copy(navigation = ScanNavigation.ToFoodDetail(result.food.id)) }

                is FoodBarcodeResult.NotFound ->
                    _uiState.update { it.copy(navigation = ScanNavigation.ToCustomFood(ean)) }

                is FoodBarcodeResult.Failure ->
                    _uiState.update { it.copy(isResolving = false, errorMessage = result.message) }
            }
        }
    }

    /** Clears the consumed navigation event after the screen has acted on it. */
    fun onNavigationHandled() {
        _uiState.update { it.copy(navigation = null) }
    }
}
