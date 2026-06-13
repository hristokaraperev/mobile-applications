package com.calorietracker.ui.customfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.food.CreateFoodRequest
import com.calorietracker.data.food.FoodCreateResult
import com.calorietracker.data.food.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the custom-food form.
 *
 * @property barcode the scanned EAN this label is for, shown read-only.
 * @property name the food name; required.
 * @property energyKcalText the per-100 g energy input, kept verbatim for the text field.
 * @property createdFoodId the saved food's id once created; the screen navigates to its detail.
 * @property errorMessage a validation or save error suitable for display, or `null`.
 */
data class CustomFoodUiState(
    val barcode: String = "",
    val name: String = "",
    val brand: String = "",
    val energyKcalText: String = "",
    val proteinText: String = "",
    val carbsText: String = "",
    val fatText: String = "",
    val isSaving: Boolean = false,
    val createdFoodId: Long? = null,
    val errorMessage: String? = null,
)

/**
 * Drives the custom-food form reached when a scanned barcode is unknown. Collects the
 * label fields (pre-filling the barcode), validates name and energy, and creates the food
 * via the API so the user can then log it from its detail screen.
 */
@HiltViewModel
class CustomFoodViewModel @Inject constructor(
    private val repository: FoodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomFoodUiState())
    val uiState: StateFlow<CustomFoodUiState> = _uiState.asStateFlow()

    /** Seeds the form with the scanned [barcode]. */
    fun prefill(barcode: String) {
        _uiState.update { it.copy(barcode = barcode) }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onBrandChange(value: String) = _uiState.update { it.copy(brand = value) }

    fun onEnergyKcalChange(value: String) = _uiState.update { it.copy(energyKcalText = value) }

    fun onProteinChange(value: String) = _uiState.update { it.copy(proteinText = value) }

    fun onCarbsChange(value: String) = _uiState.update { it.copy(carbsText = value) }

    fun onFatChange(value: String) = _uiState.update { it.copy(fatText = value) }

    /** Validates the form and, if valid, creates the food label. */
    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a name for this food.") }

            return
        }

        val energyKcal = state.energyKcalText.toDoubleOrNull()
        if (energyKcal == null || energyKcal < 0.0) {
            _uiState.update { it.copy(errorMessage = "Enter energy in kcal per 100 g.") }

            return
        }

        val request = CreateFoodRequest(
            name = state.name.trim(),
            brand = state.brand.ifBlank { null }?.trim(),
            barcode = state.barcode.ifBlank { null },
            energyKcal = energyKcal,
            proteinG = state.proteinText.toDoubleOrNull(),
            carbsG = state.carbsText.toDoubleOrNull(),
            fatG = state.fatText.toDoubleOrNull(),
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.createFood(request)) {
                is FoodCreateResult.Created ->
                    _uiState.update { it.copy(isSaving = false, createdFoodId = result.food.id) }

                is FoodCreateResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}
