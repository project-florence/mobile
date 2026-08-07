package com.florence.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.AdminUser
import com.florence.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val user: UserRepository,
) : ViewModel() {

    data class AdminUiState(
        val loading: Boolean = true,
        val error: String? = null,
        val users: List<AdminUser> = emptyList(),
        val transferring: Boolean = false,
        val message: String? = null,
        val messageIsError: Boolean = false,
    )

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = user.adminUsers()
            _uiState.update {
                it.copy(
                    loading = false,
                    users = result.getOrNull() ?: it.users,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun transfer(userId: Long, amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(transferring = true, message = null, messageIsError = false) }
            val result = user.transferCredits(userId, amount)
            _uiState.update {
                it.copy(
                    transferring = false,
                    message = result.getOrNull()?.message
                        ?: result.exceptionOrNull()?.message
                        ?: "Aktarım başarısız",
                    messageIsError = result.isFailure,
                )
            }
            if (result.isSuccess) refresh()
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
