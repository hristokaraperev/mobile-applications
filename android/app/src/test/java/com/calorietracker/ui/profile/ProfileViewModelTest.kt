package com.calorietracker.ui.profile

import com.calorietracker.data.auth.TokenStore
import com.calorietracker.data.user.UpdateUserRequest
import com.calorietracker.data.user.UserApi
import com.calorietracker.data.user.UserProfileDto
import com.calorietracker.data.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake user API returning a canned profile and echoing goal updates. */
    private class FakeUserApi(private var profile: UserProfileDto) : UserApi {
        override suspend fun me(): UserProfileDto = profile
        override suspend fun updateMe(request: UpdateUserRequest): UserProfileDto {
            profile = profile.copy(dailyKcalGoal = request.dailyKcalGoal)

            return profile
        }
    }

    /** In-memory [TokenStore] so the test can assert logout cleared it. */
    private class FakeTokenStore(private var value: String? = "jwt") : TokenStore {
        override val token: Flow<String?> get() = flowOf(value)
        override suspend fun currentToken(): String? = value
        override suspend fun saveToken(token: String) { value = token }
        override suspend fun clear() { value = null }
    }

    private fun profileDto(goal: Int? = 2200) =
        UserProfileDto(id = 1, email = "a@b.com", displayName = "Ann", dailyKcalGoal = goal)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load populates the email and current kcal goal`() = runTest(dispatcher) {
        val viewModel = ProfileViewModel(UserRepository(FakeUserApi(profileDto(goal = 2200))), FakeTokenStore())

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("a@b.com", state.email)
        assertEquals(2200, state.dailyKcalGoal)
    }

    @Test
    fun `saveGoal persists the new goal and reflects it in state`() = runTest(dispatcher) {
        val viewModel = ProfileViewModel(UserRepository(FakeUserApi(profileDto(goal = 2200))), FakeTokenStore())
        viewModel.load()
        advanceUntilIdle()

        viewModel.saveGoal(1800)
        advanceUntilIdle()

        assertEquals(1800, viewModel.uiState.value.dailyKcalGoal)
    }

    @Test
    fun `logout clears the token and signals navigation to login`() = runTest(dispatcher) {
        val tokenStore = FakeTokenStore()
        val viewModel = ProfileViewModel(UserRepository(FakeUserApi(profileDto())), tokenStore)

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(null, tokenStore.currentToken())
        assertEquals(true, viewModel.uiState.value.isLoggedOut)
    }
}
