package com.florence.app.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.core.net.ApiErrorMapper
import com.florence.app.data.model.SimulationDetailResponse
import com.florence.app.data.model.SimulationHistoryItem
import com.florence.app.data.model.SimulationResponse
import com.florence.app.data.repository.SimulationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val repo: SimulationRepository,
) : ViewModel() {

    data class SimulationUiState(
        val perDayCost: Double? = null,
        val estimating: Boolean = false,
        val estimateCost: Double? = null,
        val simulating: Boolean = false,
        val result: SimulationResponse? = null,
        val historyLoading: Boolean = false,
        val history: List<SimulationHistoryItem> = emptyList(),
        val detailLoading: Boolean = false,
        val detail: SimulationDetailResponse? = null,
        val message: String? = null,
        val messageRes: Int? = null,
        val messageIsError: Boolean = false,
    )

    private val _uiState = MutableStateFlow(SimulationUiState())
    val uiState: StateFlow<SimulationUiState> = _uiState.asStateFlow()

    init {
        refreshDailyCost()
        refreshHistory()
    }

    fun refreshDailyCost() {
        viewModelScope.launch {
            repo.dailyCost().getOrNull()?.perDayCost?.let { cost ->
                _uiState.update { it.copy(perDayCost = cost) }
            }
        }
    }

    fun estimate(ticker: String, days: Int) {
        if (ticker.isBlank() || days < 1 || days > 370) return
        viewModelScope.launch {
            _uiState.update { it.copy(estimating = true) }
            val estimate = repo.estimateCost(ticker.trim().uppercase(), days).getOrNull()?.cost
            _uiState.update { it.copy(estimating = false, estimateCost = estimate) }
        }
    }

    fun runSimulation(ticker: String, days: Int, bounds: String, target: String?) {
        if (ticker.isBlank() || days < 1 || days > 370) {
            _uiState.update { it.copy(estimating = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(simulating = true, message = null, messageRes = null, messageIsError = false)
            }
            val result = repo.simulate(
                ticker = ticker.trim().uppercase(),
                days = days,
                bounds = bounds.ifBlank { "0.05" },
                target = target?.takeIf { it.isNotBlank() },
            )
            _uiState.update { state ->
                result.fold(
                    onSuccess = { sim ->
                        state.copy(
                            simulating = false,
                            result = sim,
                            message = null,
                            messageRes = null,
                            messageIsError = false,
                        )
                    },
                    onFailure = { t ->
                        val res = ApiErrorMapper.mapApiError(t)
                        state.copy(
                            simulating = false,
                            message = t.message,
                            messageRes = res,
                            messageIsError = true,
                        )
                    },
                )
            }
            refreshHistory()
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(historyLoading = true) }
            val history = repo.history(20, 0).getOrNull() ?: emptyList()
            _uiState.update { it.copy(historyLoading = false, history = history) }
        }
    }

    fun loadDetail(simId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(detailLoading = true, detail = null, message = null, messageRes = null, messageIsError = false) }
            val result = repo.detail(simId)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { state.copy(detailLoading = false, detail = it) },
                    onFailure = { t ->
                        val res = ApiErrorMapper.mapApiError(t)
                        state.copy(
                            detailLoading = false,
                            message = t.message,
                            messageRes = res,
                            messageIsError = true,
                        )
                    },
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, messageRes = null) }
    }
}