package com.calorietracker.ui.fooddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.diary.DiaryRepository
import com.calorietracker.data.diary.MealType
import com.calorietracker.data.food.FoodDto
import com.calorietracker.data.diary.LogEntryResult
import com.calorietracker.data.food.FoodLookupResult
import com.calorietracker.data.food.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * UI state for the Food detail screen.
 *
 * @property food the loaded food (per-100 g nutrition), or `null` while loading.
 * @property quantityText the raw grams input, kept verbatim for the text field.
 * @property mealType the meal the entry will be logged to.
 * @property previewKcal kcal for the current quantity, matching the server's portion math.
 * @property saved the entry was logged; the screen should navigate back.
 */
data class FoodDetailUiState(
    val food: FoodDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val quantityText: String = DEFAULT_QUANTITY,
    val mealType: MealType = MealType.BREAKFAST,
    val previewKcal: Double = 0.0,
    val saved: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val DEFAULT_QUANTITY = "100"
    }
}

/**
 * Drives the Food detail screen: loads the food, recomputes a live kcal preview as
 * the quantity changes, and (later) logs a diary entry.
 */
@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    /** Loads [foodId] for logging to [mealType] and seeds the preview for the default serving. */
    fun load(foodId: Long, mealType: MealType) {
        _uiState.update { it.copy(isLoading = true, mealType = mealType, errorMessage = null) }
        viewModelScope.launch {
            when (val result = foodRepository.foodById(foodId)) {
                is FoodLookupResult.Success ->
                    _uiState.update {
                        it.copy(isLoading = false, food = result.food, previewKcal = preview(result.food, it.quantityText))
                    }

                is FoodLookupResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    /** Updates the grams [value] and recomputes the kcal preview. */
    fun onQuantityChange(value: String) {
        _uiState.update { it.copy(quantityText = value, previewKcal = preview(it.food, value)) }
    }

    /** Changes the meal the entry will be logged to. */
    fun onMealTypeChange(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
    }

    /** Logs the current food and quantity to the diary on [entryDate]. */
    fun save(entryDate: LocalDate) {
        val state = _uiState.value
        val food = state.food ?: return
        val quantity = state.quantityText.toDoubleOrNull()
        if (quantity == null || quantity <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Enter a quantity greater than 0.") }

            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = diaryRepository.logFood(entryDate, state.mealType, food.id, quantity)) {
                is LogEntryResult.Success ->
                    _uiState.update { it.copy(isSaving = false, saved = true) }

                is LogEntryResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    /** kcal for [grams] of [food], matching the server's per-100 g math rounded to 2 dp. */
    private fun preview(food: FoodDto?, grams: String): Double {
        val energyPer100g = food?.energyKcal ?: return 0.0
        val quantity = grams.toDoubleOrNull() ?: return 0.0

        return round2(energyPer100g * quantity / 100.0)
    }

    private fun round2(value: Double): Double = (value * 100).roundToLong() / 100.0
}
