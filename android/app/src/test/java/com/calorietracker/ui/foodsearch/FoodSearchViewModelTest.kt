package com.calorietracker.ui.foodsearch

import com.calorietracker.data.food.FoodApi
import com.calorietracker.data.food.FoodDto
import com.calorietracker.data.food.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodSearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake food API that records every search query it receives. */
    private class FakeFoodApi(
        private val results: List<FoodDto> = emptyList(),
    ) : FoodApi {
        val queries = mutableListOf<String>()
        override suspend fun search(query: String): List<FoodDto> {
            queries += query

            return results
        }

        override suspend fun getById(id: Long): FoodDto = error("not used")
    }

    private fun food(id: Long, name: String): FoodDto =
        FoodDto(id = id, name = name, type = "GENERIC", source = "CIQUAL", energyKcal = 100.0)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rapid typing debounces into a single search for the final query`() = runTest(dispatcher) {
        val api = FakeFoodApi(results = listOf(food(1, "Apple")))
        val viewModel = FoodSearchViewModel(FoodRepository(api))

        viewModel.onQueryChange("a")
        advanceTimeBy(100)
        viewModel.onQueryChange("ap")
        advanceTimeBy(100)
        viewModel.onQueryChange("app")
        advanceUntilIdle()

        assertEquals(listOf("app"), api.queries)
        val state = viewModel.uiState.value
        assertEquals(listOf("Apple"), state.results.map { it.name })
    }

    @Test
    fun `clearing the query empties results without calling the API`() = runTest(dispatcher) {
        val api = FakeFoodApi(results = listOf(food(1, "Apple")))
        val viewModel = FoodSearchViewModel(FoodRepository(api))

        viewModel.onQueryChange("apple")
        advanceUntilIdle()
        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertEquals(listOf("apple"), api.queries)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}
