package com.calorietracker.ui.recipelist

import com.calorietracker.data.recipe.NutritionDto
import com.calorietracker.data.recipe.RecipeApi
import com.calorietracker.data.recipe.RecipeDto
import com.calorietracker.data.recipe.RecipeRepository
import com.calorietracker.data.recipe.RecipeRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake recipe API returning canned recipes and recording deletions. */
    private class FakeRecipeApi(
        private val recipes: MutableList<RecipeDto>,
    ) : RecipeApi {
        override suspend fun list(): List<RecipeDto> = recipes.toList()
        override suspend fun getById(id: Long): RecipeDto = recipes.first { it.id == id }
        override suspend fun create(request: RecipeRequestDto): RecipeDto = error("not used")
        override suspend fun update(id: Long, request: RecipeRequestDto): RecipeDto = error("not used")
        override suspend fun delete(id: Long) {
            recipes.removeIf { it.id == id }
        }
    }

    private fun recipe(id: Long, name: String, perPortionKcal: Double): RecipeDto =
        RecipeDto(
            id = id,
            name = name,
            numberOfPortions = 2,
            ingredients = emptyList(),
            total = NutritionDto(kcal = perPortionKcal * 2),
            perPortion = NutritionDto(kcal = perPortionKcal),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads all recipes with their per-portion kcal`() = runTest(dispatcher) {
        val api = FakeRecipeApi(
            mutableListOf(
                recipe(1, "Chili", perPortionKcal = 420.0),
                recipe(2, "Pancakes", perPortionKcal = 310.0),
            ),
        )
        val viewModel = RecipeListViewModel(RecipeRepository(api))

        viewModel.load()
        advanceUntilIdle()

        val recipes = viewModel.uiState.value.recipes
        assertEquals(listOf("Chili", "Pancakes"), recipes.map { it.name })
        assertEquals(420.0, recipes.first { it.name == "Chili" }.perPortion.kcal!!, 0.001)
    }

    @Test
    fun `deleting a recipe removes it from the list`() = runTest(dispatcher) {
        val api = FakeRecipeApi(
            mutableListOf(
                recipe(1, "Chili", perPortionKcal = 420.0),
                recipe(2, "Pancakes", perPortionKcal = 310.0),
            ),
        )
        val viewModel = RecipeListViewModel(RecipeRepository(api))

        viewModel.load()
        advanceUntilIdle()

        viewModel.delete(1)
        advanceUntilIdle()

        assertEquals(listOf("Pancakes"), viewModel.uiState.value.recipes.map { it.name })
    }
}
