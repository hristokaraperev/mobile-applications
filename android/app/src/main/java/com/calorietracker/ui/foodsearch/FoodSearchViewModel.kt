package com.calorietracker.ui.foodsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.food.FoodDto
import com.calorietracker.data.food.FoodRepository
import com.calorietracker.data.food.FoodSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI state for the Food search screen.
 *
 * @property query the current text in the search field.
 * @property results the latest search results.
 * @property errorMessage a user-facing error from the last search, or `null`.
 */
data class FoodSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<FoodDto> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Drives the Food search screen: debounces keystrokes and queries the API,
 * exposing results, loading, and error state through [uiState].
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val repository: FoodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodSearchUiState())
    val uiState: StateFlow<FoodSearchUiState> = _uiState.asStateFlow()

    private val queryInput = MutableStateFlow("")

    init {
        queryInput
            .debounce(DEBOUNCE_MILLIS)
            .map { it.trim() }
            .distinctUntilChanged()
            .onEach { runSearch(it) }
            .launchIn(viewModelScope)
    }

    /** Records the user's [value]; the actual search runs after a debounce. */
    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        queryInput.value = value
    }

    private suspend fun runSearch(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(isLoading = false, results = emptyList(), errorMessage = null) }

            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = repository.search(query)) {
            is FoodSearchResult.Success ->
                _uiState.update { it.copy(isLoading = false, results = result.foods) }

            is FoodSearchResult.Failure ->
                _uiState.update {
                    it.copy(isLoading = false, results = emptyList(), errorMessage = result.message)
                }
        }
    }

    private companion object {
        const val DEBOUNCE_MILLIS = 300L
    }
}
