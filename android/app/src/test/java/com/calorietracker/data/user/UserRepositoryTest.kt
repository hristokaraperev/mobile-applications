package com.calorietracker.data.user

import com.calorietracker.data.common.ApiResult
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

class UserRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val contentType = "application/json".toMediaType()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
            .build()
            .create(UserApi::class.java)
        repository = UserRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getProfile returns the user profile with its kcal goal`() = runTest {
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("""{"id":1,"email":"a@b.com","displayName":"Ann","dailyKcalGoal":2200}""")
        )

        val result = repository.getProfile()

        assertTrue(result is ApiResult.Success)
        val profile = (result as ApiResult.Success).data
        assertEquals("a@b.com", profile.email)
        assertEquals(2200, profile.dailyKcalGoal)

        val recorded = server.takeRequest()
        assertEquals("/users/me", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun `updateDailyKcalGoal PUTs the new goal and returns the updated profile`() = runTest {
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("""{"id":1,"email":"a@b.com","displayName":"Ann","dailyKcalGoal":1800}""")
        )

        val result = repository.updateDailyKcalGoal(1800)

        assertTrue(result is ApiResult.Success)
        assertEquals(1800, (result as ApiResult.Success).data.dailyKcalGoal)

        val recorded = server.takeRequest()
        assertEquals("/users/me", recorded.path)
        assertEquals("PUT", recorded.method)
        assertEquals("""{"dailyKcalGoal":1800}""", recorded.body.readUtf8())
    }

    @Test
    fun `updateDailyKcalGoal maps a server error to a failure`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.updateDailyKcalGoal(1800)

        assertTrue(result is ApiResult.Failure)
    }
}
