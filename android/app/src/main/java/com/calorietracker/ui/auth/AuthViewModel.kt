package com.calorietracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorietracker.data.auth.AuthRepository
import com.calorietracker.data.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state shared by the Login and Register screens.
 *
 * @property isSubmitting a request is in flight; the form should disable submit.
 * @property isAuthenticated authentication succeeded; the UI should navigate to Diary.
 * @property errorMessage a user-facing error from the last attempt, or `null`.
 */
data class AuthUiState(
    val isSubmitting: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Drives login and registration, exposing a single [uiState] the screens render.
 * Delegates the actual work to [AuthRepository] and maps the result to UI state.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        submit { repository.login(email, password) }
    }

    fun register(email: String, password: String, displayName: String?) {
        submit { repository.register(email, password, displayName) }
    }

    private fun submit(action: suspend () -> AuthResult) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = action()) {
                is AuthResult.Success ->
                    _uiState.update { it.copy(isSubmitting = false, isAuthenticated = true) }

                is AuthResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            }
        }
    }
}
