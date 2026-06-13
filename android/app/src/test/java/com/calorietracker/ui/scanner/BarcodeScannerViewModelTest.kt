package com.calorietracker.ui.scanner

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
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeScannerViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Food API whose barcode behaviour is supplied per test, recording each EAN it sees. */
    private class FakeFoodApi(
        private val onBarcode: (String) -> FoodDto,
    ) : FoodApi {
        val barcodeCalls = mutableListOf<String>()
        override suspend fun search(query: String): List<FoodDto> = error("not used")
        override suspend fun getById(id: Long): FoodDto = error("not used")
        override suspend fun getByBarcode(ean: String): FoodDto {
            barcodeCalls += ean

            return onBarcode(ean)
        }
        override suspend fun create(request: CreateFoodRequest): FoodDto = error("not used")
    }

    private fun food(id: Long): FoodDto =
        FoodDto(id = id, name = "Nutella", type = "PACKAGED", source = "OFF", energyKcal = 539.0)

    private fun httpError(code: Int): Nothing =
        throw HttpException(Response.error<Any>(code, "".toResponseBody()))

    private fun viewModel(api: FakeFoodApi) = BarcodeScannerViewModel(FoodRepository(api))

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a recognised barcode routes to the matching food detail`() = runTest(dispatcher) {
        val api = FakeFoodApi { food(42) }
        val viewModel = viewModel(api)

        viewModel.onBarcodeDetected("3017620422003")
        advanceUntilIdle()

        val nav = viewModel.uiState.value.navigation
        assertTrue(nav is ScanNavigation.ToFoodDetail)
        assertEquals(42L, (nav as ScanNavigation.ToFoodDetail).foodId)
    }

    @Test
    fun `an unknown barcode routes to the custom food form pre-filled with it`() = runTest(dispatcher) {
        val api = FakeFoodApi { httpError(404) }
        val viewModel = viewModel(api)

        viewModel.onBarcodeDetected("0000000000000")
        advanceUntilIdle()

        val nav = viewModel.uiState.value.navigation
        assertTrue(nav is ScanNavigation.ToCustomFood)
        assertEquals("0000000000000", (nav as ScanNavigation.ToCustomFood).barcode)
    }

    @Test
    fun `repeated detections of the same scan trigger a single lookup`() = runTest(dispatcher) {
        val api = FakeFoodApi { food(42) }
        val viewModel = viewModel(api)

        viewModel.onBarcodeDetected("3017620422003")
        viewModel.onBarcodeDetected("3017620422003")
        viewModel.onBarcodeDetected("3017620422003")
        advanceUntilIdle()

        assertEquals(listOf("3017620422003"), api.barcodeCalls)
    }

    @Test
    fun `a lookup failure shows a message and lets scanning resume`() = runTest(dispatcher) {
        val api = FakeFoodApi { httpError(500) }
        val viewModel = viewModel(api)

        viewModel.onBarcodeDetected("3017620422003")
        advanceUntilIdle()

        val afterFailure = viewModel.uiState.value
        assertNull(afterFailure.navigation)
        assertTrue(afterFailure.errorMessage != null)
        assertEquals(false, afterFailure.isResolving)

        viewModel.onBarcodeDetected("3017620422003")
        advanceUntilIdle()

        assertEquals(2, api.barcodeCalls.size)
    }
}
