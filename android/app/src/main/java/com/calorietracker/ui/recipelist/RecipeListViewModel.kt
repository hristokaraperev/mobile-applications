package com.calorietracker.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.recipe.RecipeDto
import com.calorietracker.data.recipe.RecipeListResult
import com.calorietracker.data.recipe.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Recipe list screen.
 *
 * @property recipes the user's recipes, each carrying its per-portion nutrition.
 */
data class RecipeListUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipeDto> = emptyList(),
    val errorMessage: String? = null,
)

/** Loads the user's recipes and exposes them with per-portion nutrition. */
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    /** Loads the user's recipes and publishes them to [uiState]. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.list()) {
                is RecipeListResult.Success ->
                    _uiState.update { it.copy(isLoading = false, recipes = result.recipes) }

                is RecipeListResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    /** Soft-deletes the recipe with [id] and removes it from [uiState] on success. */
    fun delete(id: Long) {
        viewModelScope.launch {
            if (repository.delete(id)) {
                _uiState.update { state -> state.copy(recipes = state.recipes.filterNot { it.id == id }) }
            } else {
                _uiState.update { it.copy(errorMessage = "Could not delete this recipe. Please try again.") }
            }
        }
    }
}
