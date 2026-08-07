package com.florence.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.UserProfile
import com.florence.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditsViewModel @Inject constructor(
    private val user: UserRepository,
) : ViewModel() {

    data class CreditsUiState(
        val credits: Double? = null,
        val profile: UserProfile? = null,
        val isAdmin: Boolean = false,
    )

    private val _uiState = MutableStateFlow(CreditsUiState())
    val uiState: StateFlow<CreditsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val credits = user.credits().getOrNull()
            val profile = user.profile().getOrNull()
            _uiState.update {
                it.copy(
                    credits = credits,
                    profile = profile,
                    isAdmin = profile?.userType == "admin",
                )
            }
        }
    }
}
