package com.florence.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class AuthUiState(
    val busy: Boolean = false,
    val errorRes: Int? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.auth_login_error) }
            return
        }
        _uiState.update { it.copy(busy = true, errorRes = null) }
        viewModelScope.launch {
            val result = repo.login(username, password)
            _uiState.update {
                it.copy(busy = false, errorRes = result.exceptionOrNull()?.let(::mapError))
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        _uiState.update { it.copy(busy = true, errorRes = null) }
        viewModelScope.launch {
            val result = repo.register(username, email, password)
                .mapCatching { repo.login(username, password).getOrThrow() }
            _uiState.update {
                it.copy(busy = false, errorRes = result.exceptionOrNull()?.let(::mapError))
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }

    private fun mapError(t: Throwable): Int = when {
        t is HttpException && t.code() == 401 -> R.string.auth_login_error
        t is HttpException && t.code() == 429 -> R.string.auth_rate_limited
        t is HttpException -> R.string.auth_register_error
        t is IOException -> R.string.auth_network_error
        else -> R.string.common_error
    }
}
