package com.florence.app.presentation.avatar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.AvatarItem
import com.florence.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AvatarPickerViewModel @Inject constructor(
    private val user: UserRepository,
) : ViewModel() {

    data class AvatarUiState(
        val loading: Boolean = true,
        val avatars: List<AvatarItem> = emptyList(),
        val selectedId: String? = null,
        val applying: Boolean = false,
        val saved: String? = null,   // kaydedilen avatar id (tekrar görsel seçimi için)
        val error: Boolean = false,
    )

    private val _uiState = MutableStateFlow(AvatarUiState())
    val uiState: StateFlow<AvatarUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            val result = user.avatars()
            _uiState.update {
                it.copy(
                    loading = false,
                    avatars = result.getOrNull() ?: emptyList(),
                    error = result.isFailure,
                )
            }
        }
    }

    /** Bir avatarı seç ve backend'e kaydet (PUT /profile/avatar). Başarıda saved güncellenir. */
    fun select(avatarId: String, onSaved: () -> Unit) {
        if (_uiState.value.applying) return
        viewModelScope.launch {
            _uiState.update { it.copy(applying = true, selectedId = avatarId, error = false) }
            val result = user.updateAvatar(avatarId)
            _uiState.update {
                it.copy(
                    applying = false,
                    saved = result.fold({ it.avatarId ?: avatarId }, { null }),
                    error = result.isFailure,
                )
            }
            if (result.isSuccess) onSaved()
        }
    }
}
