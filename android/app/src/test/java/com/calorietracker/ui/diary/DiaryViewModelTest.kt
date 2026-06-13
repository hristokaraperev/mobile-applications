package com.calorietracker.ui.diary

import com.calorietracker.data.diary.DiaryApi
import com.calorietracker.data.diary.DiaryEntryDto
import com.calorietracker.data.diary.DiaryRepository
import com.calorietracker.data.diary.DiarySummaryDto
import com.calorietracker.data.diary.MealTotalsDto
import com.calorietracker.data.diary.MealType
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
class DiaryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake diary API returning canned entries and summary without any network. */
    private class FakeDiaryApi(
        private val entries: List<DiaryEntryDto> = emptyList(),
        private val summary: DiarySummaryDto,
    ) : DiaryApi {
        override suspend fun entries(date: String): List<DiaryEntryDto> = entries
        override suspend fun summary(date: String): DiarySummaryDto = summary
    }

    private fun entry(mealType: String, kcal: Double): DiaryEntryDto =
        DiaryEntryDto(
            id = "00000000-0000-0000-0000-000000000000",
            entryDate = "2026-06-13",
            mealType = mealType,
            sourceType = "FOOD",
            foodId = 1L,
            quantity = 100.0,
            kcal = kcal,
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
    fun `loads entries grouped into four meals with totals from summary`() = runTest(dispatcher) {
        val api = FakeDiaryApi(
            entries = listOf(
                entry("BREAKFAST", 200.0),
                entry("BREAKFAST", 150.0),
                entry("DINNER", 600.0),
            ),
            summary = DiarySummaryDto(
                date = "2026-06-13",
                dailyKcalGoal = 2000,
                totalKcal = 950.0,
                meals = mapOf(
                    "BREAKFAST" to MealTotalsDto(kcal = 350.0),
                    "DINNER" to MealTotalsDto(kcal = 600.0),
                ),
            ),
        )
        val viewModel = DiaryViewModel(DiaryRepository(api))

        viewModel.load(LocalDate.parse("2026-06-13"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK),
            state.meals.map { it.mealType },
        )

        val breakfast = state.meals.first { it.mealType == MealType.BREAKFAST }
        assertEquals(2, breakfast.entries.size)
        assertEquals(350.0, breakfast.kcal, 0.001)

        val lunch = state.meals.first { it.mealType == MealType.LUNCH }
        assertEquals(0, lunch.entries.size)
        assertEquals(0.0, lunch.kcal, 0.001)

        assertEquals(950.0, state.totalKcal, 0.001)
        assertEquals(2000, state.dailyKcalGoal)
    }
}
