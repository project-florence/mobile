package com.florence.app.presentation.portfolio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.PortfolioAsset
import com.florence.app.data.model.PortfolioSnapshot
import com.florence.app.data.model.PortfolioTransaction
import com.florence.app.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: PortfolioRepository,
) : ViewModel() {

    private val portfolioId: String = savedStateHandle["portfolioId"] ?: ""

    data class PortfolioDetailUiState(
        val loading: Boolean = true,
        val error: String? = null,
        val snapshot: PortfolioSnapshot? = null,
        val transactions: List<PortfolioTransaction> = emptyList(),
        val adding: Boolean = false,
        val message: String? = null,
        val messageIsError: Boolean = false,
    )

    private val _uiState = MutableStateFlow(PortfolioDetailUiState())
    val uiState: StateFlow<PortfolioDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val snapshot = repo.snapshot(portfolioId).getOrNull()
            val transactions = repo.transactions(portfolioId).getOrNull() ?: emptyList()
            _uiState.update {
                it.copy(
                    loading = false,
                    snapshot = snapshot,
                    transactions = transactions,
                    error = if (snapshot == null) "Portföy yüklenemedi" else null,
                )
            }
        }
    }

    fun addTransaction(ticker: String, type: String, quantity: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(adding = true, message = null, messageIsError = false) }
            val result = repo.addTransaction(portfolioId, ticker.trim().uppercase(), type, quantity)
            _uiState.update {
                it.copy(
                    adding = false,
                    message = if (result.isSuccess) "İşlem eklendi ✓" else (result.exceptionOrNull()?.message ?: "İşlem eklenemedi"),
                    messageIsError = result.isFailure,
                )
            }
            if (result.isSuccess) refresh()
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    val assets: List<PortfolioAsset>
        get() = _uiState.value.snapshot?.valuation?.assets ?: emptyList()
}
