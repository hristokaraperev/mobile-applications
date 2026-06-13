package com.calorietracker.ui.auth

import com.calorietracker.data.auth.AuthApi
import com.calorietracker.data.auth.AuthRepository
import com.calorietracker.data.auth.AuthResponse
import com.calorietracker.data.auth.LoginRequest
import com.calorietracker.data.auth.RegisterRequest
import com.calorietracker.data.auth.TokenStore
import com.calorietracker.data.auth.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake API that returns a canned response (or error) without any network. */
    private class FakeAuthApi(
        private val response: AuthResponse? = null,
        private val error: Throwable? = null,
    ) : AuthApi {
        override suspend fun login(request: LoginRequest): AuthResponse = result()
        override suspend fun register(request: RegisterRequest): AuthResponse = result()
        private fun result(): AuthResponse = response ?: throw error!!
    }

    private class InMemoryTokenStore : TokenStore {
        private var value: String? = null
        override val token: Flow<String?> get() = flowOf(value)
        override suspend fun currentToken(): String? = value
        override suspend fun saveToken(token: String) { value = token }
        override suspend fun clear() { value = null }
    }

    private fun viewModelWith(api: AuthApi): AuthViewModel =
        AuthViewModel(AuthRepository(api, InMemoryTokenStore()))

    private fun httpError(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType())))

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful login marks the state authenticated`() = runTest(dispatcher) {
        val api = FakeAuthApi(
            response = AuthResponse("jwt", UserDto(1, "a@b.com", "Ann"))
        )
        val viewModel = viewModelWith(api)

        viewModel.login("a@b.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `invalid credentials surface an error and stay unauthenticated`() = runTest(dispatcher) {
        val viewModel = viewModelWith(FakeAuthApi(error = httpError(401)))

        viewModel.login("a@b.com", "wrong-password")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("Invalid email or password.", state.errorMessage)
    }
}
