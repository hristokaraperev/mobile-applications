package com.calorietracker.ui.fooddetail

import com.calorietracker.data.diary.CreateDiaryEntryRequest
import com.calorietracker.data.diary.DiaryApi
import com.calorietracker.data.diary.DiaryEntryDto
import com.calorietracker.data.diary.DiaryRepository
import com.calorietracker.data.diary.DiarySummaryDto
import com.calorietracker.data.diary.MealType
import com.calorietracker.data.food.FoodApi
import com.calorietracker.data.food.FoodDto
import com.calorietracker.data.food.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FoodDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeFoodApi(private val food: FoodDto) : FoodApi {
        override suspend fun search(query: String): List<FoodDto> = error("not used")
        override suspend fun getById(id: Long): FoodDto = food
    }

    /** Diary API that records the create request it receives. */
    private class RecordingDiaryApi : DiaryApi {
        var lastCreate: CreateDiaryEntryRequest? = null
        override suspend fun entries(date: String): List<DiaryEntryDto> = emptyList()
        override suspend fun summary(date: String): DiarySummaryDto =
            DiarySummaryDto(date = "2026-06-13", totalKcal = 0.0)

        override suspend fun create(request: CreateDiaryEntryRequest): DiaryEntryDto {
            lastCreate = request

            return DiaryEntryDto(
                id = "00000000-0000-0000-0000-000000000000",
                entryDate = request.entryDate,
                mealType = request.mealType,
                sourceType = request.sourceType,
                foodId = request.foodId,
                quantity = request.quantity,
                kcal = 0.0,
            )
        }
    }

    private fun food(energyKcal: Double?): FoodDto =
        FoodDto(id = 1L, name = "Apple", type = "GENERIC", source = "CIQUAL", energyKcal = energyKcal)

    private fun viewModelFor(
        food: FoodDto,
        diaryApi: DiaryApi = RecordingDiaryApi(),
    ): FoodDetailViewModel =
        FoodDetailViewModel(
            FoodRepository(FakeFoodApi(food)),
            DiaryRepository(diaryApi),
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
    fun `loading a food previews kcal for the default 100 g serving`() = runTest(dispatcher) {
        val viewModel = viewModelFor(food(energyKcal = 52.0))

        viewModel.load(foodId = 1L, mealType = MealType.BREAKFAST)
        advanceUntilIdle()

        assertEquals(52.0, viewModel.uiState.value.previewKcal, 0.001)
    }

    @Test
    fun `changing quantity scales the kcal preview by grams over 100`() = runTest(dispatcher) {
        val viewModel = viewModelFor(food(energyKcal = 389.0))

        viewModel.load(foodId = 1L, mealType = MealType.BREAKFAST)
        advanceUntilIdle()

        viewModel.onQuantityChange("40")

        assertEquals(155.6, viewModel.uiState.value.previewKcal, 0.001)
    }

    @Test
    fun `blank quantity previews zero kcal`() = runTest(dispatcher) {
        val viewModel = viewModelFor(food(energyKcal = 52.0))

        viewModel.load(foodId = 1L, mealType = MealType.BREAKFAST)
        advanceUntilIdle()

        viewModel.onQuantityChange("")

        assertEquals(0.0, viewModel.uiState.value.previewKcal, 0.001)
    }

    @Test
    fun `saving posts a diary entry for the chosen meal and quantity`() = runTest(dispatcher) {
        val diaryApi = RecordingDiaryApi()
        val viewModel = viewModelFor(food(energyKcal = 52.0), diaryApi = diaryApi)

        viewModel.load(foodId = 1L, mealType = MealType.LUNCH)
        advanceUntilIdle()
        viewModel.onQuantityChange("150")

        viewModel.save(entryDate = LocalDate.parse("2026-06-13"))
        advanceUntilIdle()

        val request = diaryApi.lastCreate
        assertNotNull(request)
        assertEquals("2026-06-13", request!!.entryDate)
        assertEquals("LUNCH", request.mealType)
        assertEquals("FOOD", request.sourceType)
        assertEquals(1L, request.foodId)
        assertEquals(150.0, request.quantity, 0.001)

        assertTrue(viewModel.uiState.value.saved)
    }
}
