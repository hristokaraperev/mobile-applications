package com.calorietracker.ui.logportion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.diary.DiaryRepository
import com.calorietracker.data.diary.LogEntryResult
import com.calorietracker.data.diary.MealType
import com.calorietracker.data.recipe.RecipeLoadResult
import com.calorietracker.data.recipe.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * UI state for the Log-a-portion screen.
 *
 * @property perPortionKcal kcal for a single portion of the loaded recipe.
 * @property quantityText the raw portions input (e.g. "0.5", "1", "2"), kept verbatim.
 * @property previewKcal kcal for the current quantity of portions.
 * @property saved the entry was logged; the screen should navigate back.
 */
data class LogPortionUiState(
    val recipeId: Long? = null,
    val recipeName: String = "",
    val perPortionKcal: Double = 0.0,
    val quantityText: String = DEFAULT_QUANTITY,
    val mealType: MealType = MealType.BREAKFAST,
    val previewKcal: Double = 0.0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val DEFAULT_QUANTITY = "1"
    }
}

/**
 * Drives the Log-a-portion screen: loads a recipe's per-portion kcal, previews the
 * kcal for the chosen number of portions, and logs a `RECIPE_PORTION` diary entry.
 */
@HiltViewModel
class LogPortionViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogPortionUiState())
    val uiState: StateFlow<LogPortionUiState> = _uiState.asStateFlow()

    /** Loads recipe [recipeId] and seeds the preview for one portion. */
    fun load(recipeId: Long) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = recipeRepository.getById(recipeId)) {
                is RecipeLoadResult.Success ->
                    _uiState.update {
                        val perPortion = result.recipe.perPortion.kcal ?: 0.0

                        it.copy(
                            isLoading = false,
                            recipeId = result.recipe.id,
                            recipeName = result.recipe.name,
                            perPortionKcal = perPortion,
                            previewKcal = preview(perPortion, it.quantityText),
                        )
                    }

                is RecipeLoadResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    /** Updates the number of portions and recomputes the kcal preview. */
    fun onQuantityChange(value: String) {
        _uiState.update { it.copy(quantityText = value, previewKcal = preview(it.perPortionKcal, value)) }
    }

    /** Changes the meal the portion will be logged to. */
    fun onMealTypeChange(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
    }

    /** Logs the chosen number of portions to the diary on [entryDate]. */
    fun save(entryDate: LocalDate) {
        val state = _uiState.value
        val recipeId = state.recipeId ?: return
        val quantity = state.quantityText.toDoubleOrNull()
        if (quantity == null || quantity <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Enter a number of portions greater than 0.") }

            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = diaryRepository.logRecipePortion(entryDate, state.mealType, recipeId, quantity)) {
                is LogEntryResult.Success ->
                    _uiState.update { it.copy(isSaving = false, saved = true) }

                is LogEntryResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    /** kcal for [portions] of a recipe whose single portion is [perPortionKcal], rounded to 2 dp. */
    private fun preview(perPortionKcal: Double, portions: String): Double {
        val quantity = portions.toDoubleOrNull() ?: return 0.0

        return round2(perPortionKcal * quantity)
    }

    private fun round2(value: Double): Double = (value * 100).roundToLong() / 100.0
}
