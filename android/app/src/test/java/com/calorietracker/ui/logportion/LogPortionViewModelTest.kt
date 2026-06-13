package com.calorietracker.ui.logportion

import com.calorietracker.data.diary.CreateDiaryEntryRequest
import com.calorietracker.data.diary.DiaryApi
import com.calorietracker.data.diary.DiaryEntryDto
import com.calorietracker.data.diary.DiaryRepository
import com.calorietracker.data.diary.DiarySummaryDto
import com.calorietracker.data.diary.MealType
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LogPortionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake diary API recording the entry it was asked to create. */
    private class FakeDiaryApi : DiaryApi {
        val created = mutableListOf<CreateDiaryEntryRequest>()
        override suspend fun create(request: CreateDiaryEntryRequest): DiaryEntryDto {
            created += request

            return DiaryEntryDto(
                id = "00000000-0000-0000-0000-000000000000",
                entryDate = request.entryDate,
                mealType = request.mealType,
                sourceType = request.sourceType,
                recipeId = request.recipeId,
                quantity = request.quantity,
                kcal = 0.0,
            )
        }

        override suspend fun entries(date: String): List<DiaryEntryDto> = emptyList()
        override suspend fun summary(date: String): DiarySummaryDto = error("not used")
    }

    /** Fake recipe API serving a single recipe with a known per-portion kcal. */
    private class FakeRecipeApi(private val recipe: RecipeDto) : RecipeApi {
        override suspend fun list(): List<RecipeDto> = listOf(recipe)
        override suspend fun getById(id: Long): RecipeDto = recipe
        override suspend fun create(request: RecipeRequestDto): RecipeDto = error("not used")
        override suspend fun update(id: Long, request: RecipeRequestDto): RecipeDto = error("not used")
        override suspend fun delete(id: Long) = Unit
    }

    private fun recipe(id: Long, name: String, perPortionKcal: Double): RecipeDto =
        RecipeDto(
            id = id,
            name = name,
            numberOfPortions = 2,
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
    fun `loading shows the recipe and a one-portion kcal preview`() = runTest(dispatcher) {
        val viewModel = LogPortionViewModel(
            DiaryRepository(FakeDiaryApi()),
            RecipeRepository(FakeRecipeApi(recipe(7, "Chili", perPortionKcal = 420.0))),
        )

        viewModel.load(7)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Chili", state.recipeName)
        assertEquals(420.0, state.previewKcal, 0.001)
    }

    @Test
    fun `changing the quantity scales the kcal preview`() = runTest(dispatcher) {
        val viewModel = LogPortionViewModel(
            DiaryRepository(FakeDiaryApi()),
            RecipeRepository(FakeRecipeApi(recipe(7, "Chili", perPortionKcal = 420.0))),
        )
        viewModel.load(7)
        advanceUntilIdle()

        viewModel.onQuantityChange("0.5")

        assertEquals(210.0, viewModel.uiState.value.previewKcal, 0.001)
    }

    @Test
    fun `logging a portion creates a RECIPE_PORTION diary entry`() = runTest(dispatcher) {
        val diaryApi = FakeDiaryApi()
        val viewModel = LogPortionViewModel(
            DiaryRepository(diaryApi),
            RecipeRepository(FakeRecipeApi(recipe(7, "Chili", perPortionKcal = 420.0))),
        )
        viewModel.load(7)
        advanceUntilIdle()
        viewModel.onMealTypeChange(MealType.DINNER)
        viewModel.onQuantityChange("2")

        viewModel.save(LocalDate.parse("2026-06-14"))
        advanceUntilIdle()

        val request = diaryApi.created.single()
        assertEquals("RECIPE_PORTION", request.sourceType)
        assertEquals(7L, request.recipeId)
        assertEquals(2.0, request.quantity, 0.001)
        assertEquals("DINNER", request.mealType)
        assertEquals("2026-06-14", request.entryDate)
        assertEquals(true, viewModel.uiState.value.saved)
    }
}
