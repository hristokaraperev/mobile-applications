package com.calorietracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.auth.TokenStore
import com.calorietracker.data.common.ApiResult
import com.calorietracker.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Profile screen.
 *
 * @property isLoading the profile is being fetched.
 * @property email the user's email, shown read-only; `null` until loaded.
 * @property dailyKcalGoal the user's current goal, or `null` if unset.
 * @property isSaving a goal update is in flight.
 * @property isLoggedOut logout completed; the UI should navigate to Login.
 * @property errorMessage a user-facing error from the last action, or `null`.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val email: String? = null,
    val dailyKcalGoal: Int? = null,
    val isSaving: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Drives the Profile screen: loads the user's profile, persists changes to the
 * daily kcal goal, and clears the stored token on logout.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Loads the current user's profile into [uiState]. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.getProfile()) {
                is ApiResult.Success ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = result.data.email,
                            dailyKcalGoal = result.data.dailyKcalGoal,
                        )
                    }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    /** Persists [goal] as the new daily kcal goal, updating [uiState] on success. */
    fun saveGoal(goal: Int) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.updateDailyKcalGoal(goal)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(isSaving = false, dailyKcalGoal = result.data.dailyKcalGoal) }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    /** Clears the stored token and signals the UI to return to Login. */
    fun logout() {
        viewModelScope.launch {
            tokenStore.clear()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
