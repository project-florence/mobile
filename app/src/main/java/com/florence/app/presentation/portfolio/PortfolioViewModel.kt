package com.florence.app.presentation.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.Portfolio
import com.florence.app.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val repo: PortfolioRepository,
) : ViewModel() {

    data class PortfolioUiState(
        val loading: Boolean = true,
        val error: String? = null,
        val portfolios: List<Portfolio> = emptyList(),
        val creating: Boolean = false,
        val createError: String? = null,
    )

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.portfolios()
            _uiState.update {
                it.copy(
                    loading = false,
                    portfolios = result.getOrNull() ?: it.portfolios,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun createPortfolio(name: String, initialBalance: Double) {
        if (name.isBlank() || initialBalance <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(creating = true, createError = null) }
            val result = repo.createPortfolio(name.trim(), initialBalance)
            _uiState.update {
                it.copy(
                    creating = false,
                    createError = result.exceptionOrNull()?.message,
                    portfolios = if (result.isSuccess) {
                        it.portfolios + listOfNotNull(result.getOrNull())
                    } else {
                        it.portfolios
                    },
                )
            }
        }
    }

    fun clearCreateError() {
        _uiState.update { it.copy(createError = null) }
    }
}
