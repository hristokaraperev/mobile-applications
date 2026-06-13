package com.calorietracker.data.auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var repository: AuthRepository

    /** In-memory [TokenStore] so the test can assert what the repository persisted. */
    private class FakeTokenStore : TokenStore {
        private var value: String? = null
        override val token: Flow<String?> get() = flowOf(value)
        override suspend fun currentToken(): String? = value
        override suspend fun saveToken(token: String) { value = token }
        override suspend fun clear() { value = null }
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val contentType = "application/json".toMediaType()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
            .build()
            .create(AuthApi::class.java)
        tokenStore = FakeTokenStore()
        repository = AuthRepository(api, tokenStore)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `login returns the user and persists the access token`() = runTest {
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("""{"accessToken":"jwt-xyz","user":{"id":1,"email":"a@b.com","displayName":"Ann"}}""")
        )

        val result = repository.login("a@b.com", "password123")

        assertTrue(result is AuthResult.Success)
        assertEquals("a@b.com", (result as AuthResult.Success).user.email)
        assertEquals("jwt-xyz", tokenStore.currentToken())

        val recorded = server.takeRequest()
        assertEquals("/auth/login", recorded.path)
    }

    @Test
    fun `login with invalid credentials fails and stores no token`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"message":"Invalid email or password"}""")
        )

        val result = repository.login("a@b.com", "wrong-password")

        assertTrue(result is AuthResult.Failure)
        assertNull(tokenStore.currentToken())
    }

    @Test
    fun `register returns the user and persists the access token`() = runTest {
        server.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("""{"accessToken":"jwt-new","user":{"id":7,"email":"new@b.com","displayName":"New User"}}""")
        )

        val result = repository.register("new@b.com", "password123", "New User")

        assertTrue(result is AuthResult.Success)
        assertEquals("new@b.com", (result as AuthResult.Success).user.email)
        assertEquals("jwt-new", tokenStore.currentToken())

        val recorded = server.takeRequest()
        assertEquals("/auth/register", recorded.path)
    }
}
