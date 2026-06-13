package com.calorietracker.ui.recipeeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.food.FoodDto
import com.calorietracker.data.food.FoodLookupResult
import com.calorietracker.data.food.FoodRepository
import com.calorietracker.data.recipe.NutritionDto
import com.calorietracker.data.recipe.RecipeLoadResult
import com.calorietracker.data.recipe.RecipeIngredientDto
import com.calorietracker.data.recipe.RecipeRepository
import com.calorietracker.data.recipe.RecipeRequestDto
import com.calorietracker.data.recipe.RecipeSaveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

/** A chosen ingredient: the food (per-100 g nutrition) and the grams used. */
data class EditorIngredient(
    val food: FoodDto,
    val grams: Double,
)

/**
 * UI state for the Recipe editor screen.
 *
 * @property recipeId the recipe being edited, or `null` when creating a new one.
 * @property total nutrition summed across all ingredients, recomputed on every change.
 * @property perPortion [total] divided by the portion count, recomputed on every change.
 */
data class RecipeEditorUiState(
    val recipeId: Long? = null,
    val name: String = "",
    val portionsText: String = DEFAULT_PORTIONS,
    val ingredients: List<EditorIngredient> = emptyList(),
    val total: NutritionDto = NutritionDto(),
    val perPortion: NutritionDto = NutritionDto(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val DEFAULT_PORTIONS = "1"
    }
}

/**
 * Drives the Recipe editor: holds the working recipe, recomputes live total and
 * per-portion nutrition as ingredients or the portion count change, and saves via
 * the API (creating or updating).
 */
@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeEditorUiState())
    val uiState: StateFlow<RecipeEditorUiState> = _uiState.asStateFlow()

    /**
     * Loads the recipe [recipeId] for editing, hydrating each ingredient's food so live
     * nutrition can be recomputed as the user edits.
     */
    fun load(recipeId: Long) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = recipeRepository.getById(recipeId)) {
                is RecipeLoadResult.Success -> {
                    val recipe = result.recipe
                    val ingredients = mutableListOf<EditorIngredient>()
                    for (line in recipe.ingredients) {
                        when (val food = foodRepository.foodById(line.foodId)) {
                            is FoodLookupResult.Success -> ingredients += EditorIngredient(food.food, line.grams)
                            is FoodLookupResult.Failure ->
                                _uiState.update { it.copy(errorMessage = food.message) }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recipeId = recipe.id,
                            name = recipe.name,
                            portionsText = recipe.numberOfPortions.toString(),
                            ingredients = ingredients,
                        ).recomputed()
                    }
                }

                is RecipeLoadResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    /** Updates the recipe name. */
    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    /** Updates the portion count and rescales per-portion nutrition. */
    fun onPortionsChange(value: String) {
        _uiState.update { it.copy(portionsText = value).recomputed() }
    }

    /** Adds [grams] of [food] as an ingredient and recomputes nutrition. */
    fun addIngredient(food: FoodDto, grams: Double) {
        _uiState.update { it.copy(ingredients = it.ingredients + EditorIngredient(food, grams)).recomputed() }
    }

    /**
     * Adds [grams] of the food [foodId] as an ingredient, fetching its nutrition first.
     * Used when returning from the reused food-search flow, which yields only an id.
     */
    fun addIngredientById(foodId: Long, grams: Double) {
        viewModelScope.launch {
            when (val result = foodRepository.foodById(foodId)) {
                is FoodLookupResult.Success -> addIngredient(result.food, grams)
                is FoodLookupResult.Failure -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    /** Removes the ingredient at [index] and recomputes nutrition. */
    fun removeIngredient(index: Int) {
        _uiState.update {
            it.copy(ingredients = it.ingredients.filterIndexed { i, _ -> i != index }).recomputed()
        }
    }

    /**
     * Saves the recipe: POSTs a new one when [RecipeEditorUiState.recipeId] is `null`,
     * otherwise PUTs the existing recipe. Sets [RecipeEditorUiState.saved] on success.
     */
    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Give your recipe a name.") }

            return
        }
        if (state.ingredients.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add at least one ingredient.") }

            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val request = RecipeRequestDto(
                name = state.name,
                numberOfPortions = state.portionsText.toIntOrNull() ?: 1,
                ingredients = state.ingredients.map { RecipeIngredientDto(it.food.id, it.grams) },
            )

            val result = state.recipeId
                ?.let { recipeRepository.update(it, request) }
                ?: recipeRepository.create(request)

            when (result) {
                is RecipeSaveResult.Success ->
                    _uiState.update { it.copy(isSaving = false, saved = true, recipeId = result.recipe.id) }

                is RecipeSaveResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    /** Recomputes [total] and [perPortion] from the current ingredients and portion count. */
    private fun RecipeEditorUiState.recomputed(): RecipeEditorUiState {
        var kcal = 0.0
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0
        for (ingredient in ingredients) {
            val factor = ingredient.grams / 100.0
            kcal += (ingredient.food.energyKcal ?: 0.0) * factor
            protein += (ingredient.food.proteinG ?: 0.0) * factor
            carbs += (ingredient.food.carbsG ?: 0.0) * factor
            fat += (ingredient.food.fatG ?: 0.0) * factor
        }

        val portions = portionsText.toIntOrNull()?.takeIf { it > 0 } ?: 1
        val total = NutritionDto(round2(kcal), round2(protein), round2(carbs), round2(fat))
        val perPortion = NutritionDto(
            round2(kcal / portions),
            round2(protein / portions),
            round2(carbs / portions),
            round2(fat / portions),
        )

        return copy(total = total, perPortion = perPortion)
    }

    private fun round2(value: Double): Double = (value * 100).roundToLong() / 100.0
}
