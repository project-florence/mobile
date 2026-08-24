package com.florence.app.presentation.ipo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.core.net.ApiErrorMapper
import com.florence.app.data.model.IpoDetail
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IpoDetailUiState(
    val loading: Boolean = true,
    val detail: IpoDetail? = null,
    /** Kullanıcıya gösterilecek hata string kaynağı — null ise hata yok. */
    val errorRes: Int? = null,
)

@HiltViewModel
class IpoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MiscRepository,
) : ViewModel() {

    val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow(IpoDetailUiState())
    val uiState: StateFlow<IpoDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorRes = null) }
            val result = repo.ipoDetail(slug)
            _uiState.update {
                it.copy(
                    loading = false,
                    detail = result.getOrNull(),
                    errorRes = result.exceptionOrNull()?.let(::mapIpoError),
                )
            }
        }
    }

    private fun mapIpoError(t: Throwable): Int =
        ApiErrorMapper.mapApiError(t) ?: R.string.ipo_error
}