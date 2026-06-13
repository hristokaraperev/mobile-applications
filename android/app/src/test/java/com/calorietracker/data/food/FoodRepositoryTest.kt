package com.calorietracker.data.food

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class FoodRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: FoodRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val contentType = "application/json".toMediaType()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
            .build()
            .create(FoodApi::class.java)
        repository = FoodRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `barcode lookup returns the matching food on a hit`() = runTest {
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """{"id":42,"name":"Nutella","barcode":"3017620422003",
                       "type":"PACKAGED","source":"OFF","energyKcal":539.0}"""
                )
        )

        val result = repository.foodByBarcode("3017620422003")

        assertTrue(result is FoodBarcodeResult.Found)
        assertEquals(42L, (result as FoodBarcodeResult.Found).food.id)

        val recorded = server.takeRequest()
        assertEquals("/foods/barcode/3017620422003", recorded.path)
    }

    @Test
    fun `barcode lookup reports NotFound on a 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository.foodByBarcode("0000000000000")

        assertTrue(result is FoodBarcodeResult.NotFound)
    }

    @Test
    fun `barcode lookup reports failure on a server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.foodByBarcode("3017620422003")

        assertTrue(result is FoodBarcodeResult.Failure)
    }

    @Test
    fun `creating a food posts the label and returns the saved food`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """{"id":7,"name":"My Snack","barcode":"123","type":"PACKAGED",
                       "source":"USER","energyKcal":250.0}"""
                )
        )

        val result = repository.createFood(
            CreateFoodRequest(name = "My Snack", barcode = "123", energyKcal = 250.0)
        )

        assertTrue(result is FoodCreateResult.Created)
        assertEquals(7L, (result as FoodCreateResult.Created).food.id)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/foods", recorded.path)
        assertTrue(recorded.body.readUtf8().contains("\"barcode\":\"123\""))
    }

    @Test
    fun `creating a food reports failure on a server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400))

        val result = repository.createFood(
            CreateFoodRequest(name = "My Snack", energyKcal = 250.0)
        )

        assertTrue(result is FoodCreateResult.Failure)
    }
}
