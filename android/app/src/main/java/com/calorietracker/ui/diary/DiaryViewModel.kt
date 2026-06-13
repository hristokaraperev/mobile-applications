package com.calorietracker.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.diary.DiaryDay
import com.calorietracker.data.diary.DiaryEntryDto
import com.calorietracker.data.diary.DiaryRepository
import com.calorietracker.data.diary.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * One meal section of the diary: its entries and the kcal total reported by the
 * daily summary (which may differ from summing [entries] due to server rounding).
 */
data class MealSection(
    val mealType: MealType,
    val entries: List<DiaryEntryDto>,
    val kcal: Double,
)

/**
 * UI state for the Diary screen.
 *
 * @property date the day currently displayed.
 * @property meals the four meal sections, always present and in order.
 * @property totalKcal the daily total from the summary.
 * @property dailyKcalGoal the user's goal, or `null` if unset.
 */
data class DiaryUiState(
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val meals: List<MealSection> = emptyList(),
    val totalKcal: Double = 0.0,
    val dailyKcalGoal: Int? = null,
    val errorMessage: String? = null,
)

/** Loads and exposes a day's diary, grouping entries into the four meal sections. */
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    /** Loads the diary for [date] and publishes it to [uiState]. */
    fun load(date: LocalDate) {
        _uiState.update { it.copy(date = date, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val day = repository.loadDay(date)
            _uiState.update { it.copy(isLoading = false, date = day.date).withDay(day) }
        }
    }

    /** Reloads the diary for the day before the one currently shown. */
    fun previousDay() {
        load(_uiState.value.date.minusDays(1))
    }

    /** Reloads the diary for the day after the one currently shown. */
    fun nextDay() {
        load(_uiState.value.date.plusDays(1))
    }

    private fun DiaryUiState.withDay(day: DiaryDay): DiaryUiState {
        val byMeal = day.entries.groupBy { it.mealType }

        val sections = MealType.entries.map { meal ->
            MealSection(
                mealType = meal,
                entries = byMeal[meal.name].orEmpty(),
                kcal = day.summary.meals[meal.name]?.kcal ?: 0.0,
            )
        }

        return copy(
            meals = sections,
            totalKcal = day.summary.totalKcal,
            dailyKcalGoal = day.summary.dailyKcalGoal,
        )
    }
}
