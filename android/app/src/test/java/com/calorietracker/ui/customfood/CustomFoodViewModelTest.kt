package com.calorietracker.ui.customfood

import com.calorietracker.data.food.CreateFoodRequest
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomFoodViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Food API recording the create request it receives. */
    private class RecordingFoodApi(private val saved: FoodDto) : FoodApi {
        var lastCreate: CreateFoodRequest? = null
        override suspend fun search(query: String): List<FoodDto> = error("not used")
        override suspend fun getById(id: Long): FoodDto = error("not used")
        override suspend fun getByBarcode(ean: String): FoodDto = error("not used")
        override suspend fun create(request: CreateFoodRequest): FoodDto {
            lastCreate = request

            return saved
        }
    }

    private fun savedFood(id: Long) =
        FoodDto(id = id, name = "My Snack", type = "PACKAGED", source = "USER", energyKcal = 250.0)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `prefills the scanned barcode`() {
        val viewModel = CustomFoodViewModel(FoodRepository(RecordingFoodApi(savedFood(1))))

        viewModel.prefill("3017620422003")

        assertEquals("3017620422003", viewModel.uiState.value.barcode)
    }

    @Test
    fun `saving a valid label creates the food and routes to its detail`() = runTest(dispatcher) {
        val api = RecordingFoodApi(savedFood(7))
        val viewModel = CustomFoodViewModel(FoodRepository(api))
        viewModel.prefill("123")
        viewModel.onNameChange("My Snack")
        viewModel.onEnergyKcalChange("250")

        viewModel.save()
        advanceUntilIdle()

        assertEquals("My Snack", api.lastCreate?.name)
        assertEquals(250.0, api.lastCreate?.energyKcal)
        assertEquals("123", api.lastCreate?.barcode)
        assertEquals(7L, viewModel.uiState.value.createdFoodId)
    }

    @Test
    fun `saving without a name shows an error and does not call the API`() = runTest(dispatcher) {
        val api = RecordingFoodApi(savedFood(7))
        val viewModel = CustomFoodViewModel(FoodRepository(api))
        viewModel.onEnergyKcalChange("250")

        viewModel.save()
        advanceUntilIdle()

        assertNull(api.lastCreate)
        assertNull(viewModel.uiState.value.createdFoodId)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }
}
