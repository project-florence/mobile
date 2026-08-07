package com.florence.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.ReportHistoryItem
import com.florence.app.data.model.ReportsInfoResponse
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repo: MiscRepository,
) : ViewModel() {

    data class ReportsUiState(
        val loading: Boolean = true,
        val history: List<ReportHistoryItem> = emptyList(),
        val info: ReportsInfoResponse? = null,
        val generating: Boolean = false,
        val message: String? = null,
        val messageIsError: Boolean = false,
    )

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val history = repo.reportsHistory().getOrNull() ?: emptyList()
            val info = repo.reportsInfo().getOrNull()
            _uiState.update { it.copy(loading = false, history = history, info = info) }
        }
    }

    fun generate(ticker: String, reportType: String) {
        if (ticker.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(generating = true, message = null, messageIsError = false) }
            val result = repo.generateReport(ticker.trim().uppercase(), reportType)
            _uiState.update {
                it.copy(
                    generating = false,
                    message = if (result.isSuccess) {
                        "Rapor kuyruğa alındı. Geçmiş sekmesinden takip edebilirsin."
                    } else {
                        result.exceptionOrNull()?.message ?: "Rapor oluşturulamadı."
                    },
                    messageIsError = result.isFailure,
                )
            }
            refresh()
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
