package com.florence.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class NotificationViewModel @Inject constructor(
    private val misc: MiscRepository,
) : ViewModel() {

    data class NotificationUiState(
        val loading: Boolean = true,
        val error: Boolean = false,
        val announcements: List<AnnouncementItem> = emptyList(),
        val readIds: Set<Long> = emptySet(),
    ) {
        fun isRead(item: AnnouncementItem): Boolean =
            (item.id ?: -1L) in readIds || !item.isUnread
    }

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            val result = misc.announcements()
            _uiState.update { state ->
                result.fold(
                    onSuccess = { list ->
                        state.copy(loading = false, error = false, announcements = list)
                    },
                    onFailure = {
                        state.copy(loading = false, error = true)
                    },
                )
            }
        }
    }

    /** Backend kullanıcı-tarafı okundu ucu yok (admin-only) → yalnızca görsel, oturum-içi. */
    fun markRead(item: AnnouncementItem) {
        val id = item.id ?: return
        _uiState.update { state ->
            state.copy(readIds = state.readIds + id)
        }
    }
}
