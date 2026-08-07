package com.florence.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.core.storage.TokenStore
import com.florence.app.data.model.AnnouncementItem
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val misc: MiscRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    data class ProfileUiState(
        val loading: Boolean = true,
        val announcements: List<AnnouncementItem> = emptyList(),
        val loggedIn: Boolean = true,
        val loggingOut: Boolean = false,
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = misc.announcements()
            _uiState.update {
                it.copy(loading = false, announcements = result.getOrNull() ?: emptyList())
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loggingOut = true) }
            tokenStore.clear()
            _uiState.update { it.copy(loggingOut = false, loggedIn = false) }
            onDone()
        }
    }
}
