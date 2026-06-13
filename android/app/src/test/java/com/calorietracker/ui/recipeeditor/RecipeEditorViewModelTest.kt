package com.calorietracker.ui.recipeeditor

import com.calorietracker.data.food.CreateFoodRequest
import com.calorietracker.data.food.FoodApi
import com.calorietracker.data.food.FoodDto
import com.calorietracker.data.food.FoodRepository
import com.calorietracker.data.recipe.RecipeApi
import com.calorietracker.data.recipe.RecipeDto
import com.calorietracker.data.recipe.RecipeRepository
import com.calorietracker.data.recipe.RecipeIngredientDto
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
class RecipeEditorViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake recipe API recording create/update requests and serving canned recipes. */
    private class FakeRecipeApi(
        private val recipesById: Map<Long, RecipeDto> = emptyMap(),
    ) : RecipeApi {
        val created = mutableListOf<RecipeRequestDto>()
        val updated = mutableListOf<Pair<Long, RecipeRequestDto>>()

        override suspend fun list(): List<RecipeDto> = recipesById.values.toList()
        override suspend fun getById(id: Long): RecipeDto = recipesById.getValue(id)
        override suspend fun create(request: RecipeRequestDto): RecipeDto {
            created += request

            return RecipeDto(id = 99, name = request.name, numberOfPortions = request.numberOfPortions)
        }

        override suspend fun update(id: Long, request: RecipeRequestDto): RecipeDto {
            updated += id to request

            return RecipeDto(id = id, name = request.name, numberOfPortions = request.numberOfPortions)
        }

        override suspend fun delete(id: Long) = Unit
    }

    /** Fake food API serving canned foods by id for ingredient hydration. */
    private class FakeFoodApi(private val foodsById: Map<Long, FoodDto>) : FoodApi {
        override suspend fun search(query: String): List<FoodDto> = emptyList()
        override suspend fun getById(id: Long): FoodDto = foodsById.getValue(id)
        override suspend fun getByBarcode(ean: String): FoodDto = error("not used")
        override suspend fun create(request: CreateFoodRequest): FoodDto = error("not used")
    }

    private fun food(
        id: Long,
        name: String,
        kcal: Double,
        proteinG: Double = 0.0,
    ): FoodDto = FoodDto(
        id = id,
        name = name,
        type = "GENERIC",
        source = "CIQUAL",
        energyKcal = kcal,
        proteinG = proteinG,
    )

    private fun viewModel(
        recipeApi: RecipeApi = FakeRecipeApi(),
        foodApi: FoodApi = FakeFoodApi(emptyMap()),
    ) = RecipeEditorViewModel(RecipeRepository(recipeApi), FoodRepository(foodApi))

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adding an ingredient updates total and per-portion nutrition`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onPortionsChange("2")

        viewModel.addIngredient(food(1, "Rice", kcal = 130.0, proteinG = 2.7), grams = 200.0)

        val state = viewModel.uiState.value
        assertEquals(260.0, state.total.kcal!!, 0.001)
        assertEquals(5.4, state.total.proteinG!!, 0.001)
        assertEquals(130.0, state.perPortion.kcal!!, 0.001)
        assertEquals(2.7, state.perPortion.proteinG!!, 0.001)
    }

    @Test
    fun `changing the portion count rescales per-portion nutrition`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onPortionsChange("2")
        viewModel.addIngredient(food(1, "Rice", kcal = 130.0), grams = 200.0)

        viewModel.onPortionsChange("4")

        val state = viewModel.uiState.value
        assertEquals(260.0, state.total.kcal!!, 0.001)
        assertEquals(65.0, state.perPortion.kcal!!, 0.001)
    }

    @Test
    fun `removing an ingredient updates the nutrition totals`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onPortionsChange("1")
        viewModel.addIngredient(food(1, "Rice", kcal = 130.0), grams = 200.0)
        viewModel.addIngredient(food(2, "Oil", kcal = 900.0), grams = 100.0)

        viewModel.removeIngredient(1)

        val state = viewModel.uiState.value
        assertEquals(1, state.ingredients.size)
        assertEquals(260.0, state.total.kcal!!, 0.001)
    }

    @Test
    fun `saving a new recipe creates it via POST and marks the editor saved`() = runTest(dispatcher) {
        val api = FakeRecipeApi()
        val viewModel = viewModel(recipeApi = api)
        viewModel.onNameChange("Chili")
        viewModel.onPortionsChange("2")
        viewModel.addIngredient(food(1, "Beans", kcal = 130.0), grams = 200.0)

        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, api.created.size)
        assertEquals(0, api.updated.size)
        val request = api.created.single()
        assertEquals("Chili", request.name)
        assertEquals(2, request.numberOfPortions)
        assertEquals(listOf(1L), request.ingredients.map { it.foodId })
        assertEquals(listOf(200.0), request.ingredients.map { it.grams })
        assertEquals(true, viewModel.uiState.value.saved)
    }

    @Test
    fun `loading an existing recipe hydrates ingredients so nutrition recomputes`() = runTest(dispatcher) {
        val recipeApi = FakeRecipeApi(
            recipesById = mapOf(
                5L to RecipeDto(
                    id = 5,
                    name = "Stew",
                    numberOfPortions = 2,
                    ingredients = listOf(
                        RecipeIngredientDto(foodId = 1, grams = 200.0),
                        RecipeIngredientDto(foodId = 2, grams = 100.0),
                    ),
                ),
            ),
        )
        val foodApi = FakeFoodApi(
            mapOf(
                1L to food(1, "Beans", kcal = 130.0),
                2L to food(2, "Oil", kcal = 900.0),
            ),
        )
        val viewModel = viewModel(recipeApi = recipeApi, foodApi = foodApi)

        viewModel.load(5)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Stew", state.name)
        assertEquals("2", state.portionsText)
        assertEquals(listOf("Beans", "Oil"), state.ingredients.map { it.food.name })
        assertEquals(1160.0, state.total.kcal!!, 0.001)
        assertEquals(580.0, state.perPortion.kcal!!, 0.001)
    }

    @Test
    fun `saving a loaded recipe updates it via PUT`() = runTest(dispatcher) {
        val recipeApi = FakeRecipeApi(
            recipesById = mapOf(
                5L to RecipeDto(
                    id = 5,
                    name = "Stew",
                    numberOfPortions = 2,
                    ingredients = listOf(RecipeIngredientDto(foodId = 1, grams = 200.0)),
                ),
            ),
        )
        val foodApi = FakeFoodApi(mapOf(1L to food(1, "Beans", kcal = 130.0)))
        val viewModel = viewModel(recipeApi = recipeApi, foodApi = foodApi)
        viewModel.load(5)
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        assertEquals(0, recipeApi.created.size)
        assertEquals(1, recipeApi.updated.size)
        assertEquals(5L, recipeApi.updated.single().first)
        assertEquals(listOf(1L), recipeApi.updated.single().second.ingredients.map { it.foodId })
    }

    @Test
    fun `adding an ingredient by id hydrates the food and updates nutrition`() = runTest(dispatcher) {
        val foodApi = FakeFoodApi(mapOf(3L to food(3, "Tomato", kcal = 18.0)))
        val viewModel = viewModel(foodApi = foodApi)
        viewModel.onPortionsChange("1")

        viewModel.addIngredientById(foodId = 3, grams = 200.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("Tomato"), state.ingredients.map { it.food.name })
        assertEquals(36.0, state.total.kcal!!, 0.001)
    }

    @Test
    fun `saving with a blank name reports an error without calling the API`() = runTest(dispatcher) {
        val api = FakeRecipeApi()
        val viewModel = viewModel(recipeApi = api)
        viewModel.onNameChange("   ")
        viewModel.addIngredient(food(1, "Beans", kcal = 130.0), grams = 200.0)

        viewModel.save()
        advanceUntilIdle()

        assertEquals(0, api.created.size)
        assertEquals(false, viewModel.uiState.value.saved)
        assertEquals(true, viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `saving with no ingredients reports an error without calling the API`() = runTest(dispatcher) {
        val api = FakeRecipeApi()
        val viewModel = viewModel(recipeApi = api)
        viewModel.onNameChange("Empty")

        viewModel.save()
        advanceUntilIdle()

        assertEquals(0, api.created.size)
        assertEquals(false, viewModel.uiState.value.saved)
        assertEquals(true, viewModel.uiState.value.errorMessage != null)
    }
}
