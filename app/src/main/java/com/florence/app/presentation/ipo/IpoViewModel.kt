package com.florence.app.presentation.ipo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.core.net.ApiErrorMapper
import com.florence.app.data.model.IpoItem
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class IpoTab {
    UPCOMING,
    ACTIVE,
    DRAFT,
}

data class IpoUiState(
    val loading: Boolean = true,
    val currentTab: IpoTab = IpoTab.ACTIVE,
    val lists: Map<IpoTab, List<IpoItem>> = emptyMap(),
    /** Kullanıcıya gösterilecek hata string kaynağı — null ise hata yok. */
    val errorRes: Int? = null,
) {
    val currentList: List<IpoItem> get() = lists[currentTab] ?: emptyList()
}

@HiltViewModel
class IpoViewModel @Inject constructor(
    private val repo: MiscRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IpoUiState())
    val uiState: StateFlow<IpoUiState> = _uiState.asStateFlow()

    init {
        loadTab(IpoTab.ACTIVE)
    }

    fun selectTab(tab: IpoTab) {
        if (_uiState.value.currentTab == tab) return
        _uiState.update { it.copy(currentTab = tab, errorRes = null) }
        // Bu sekme daha önce yüklenmediyse çek.
        if (!_uiState.value.lists.containsKey(tab)) loadTab(tab)
    }

    /** Hata ekranındaki "Tekrar dene" — aktif sekmeyi yeniden yükler. */
    fun retry() {
        loadTab(_uiState.value.currentTab)
    }

    private fun loadTab(tab: IpoTab) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorRes = null) }
            val result = when (tab) {
                IpoTab.UPCOMING -> repo.upcomingIpos()
                IpoTab.ACTIVE -> repo.ipos()
                IpoTab.DRAFT -> repo.draftIpos()
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    lists = it.lists + (tab to (result.getOrNull() ?: emptyList())),
                    errorRes = result.exceptionOrNull()?.let(::mapIpoError),
                )
            }
        }
    }

    private fun mapIpoError(t: Throwable): Int =
        ApiErrorMapper.mapApiError(t) ?: R.string.ipo_error
}