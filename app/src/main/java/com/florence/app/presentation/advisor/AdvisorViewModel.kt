package com.florence.app.presentation.advisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.core.net.ApiErrorMapper
import com.florence.app.data.model.FitRequest
import com.florence.app.data.model.FitResultItem
import com.florence.app.data.model.ReportsInfoResponse
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * G2 — AI Danışman: risk iştahı + ufuk + karlılık tercihlerini backend'in
 * risk bazlı vektör eşleştirme ucu (/stocks/fit) ile hisse önerisine dönüştürür,
 * ardından seçilen hisse için hızlı rapor üretir.
 *
 * Not: Backend'de ayrı bir "advisor" ucu YOK; advisor feature'ı /stocks/fit
 * ve /portfolio/profile üzerinden sunulur. reports/generate'da purpose alanı
 * da yoktur (yalnızca ticker + type). Bu yüzden öneri akışını /stocks/fit ile,
 * raporu ise mevcut quick_report ile bağladık.
 */
@HiltViewModel
class AdvisorViewModel @Inject constructor(
    private val repo: MiscRepository,
) : ViewModel() {

    // risk iştahı görüntü değerleri → backend risk_tolerance
    companion object {
        const val RISK_CONSERVATIVE = "low"
        const val RISK_BALANCED = "medium"
        const val RISK_AGGRESSIVE = "high"
    }

    data class AdvisorUiState(
        val riskAppetite: String = RISK_BALANCED,
        val horizon: String = "long",
        val profitability: String = "high",
        val fitting: Boolean = false,
        val recommendations: List<FitResultItem> = emptyList(),
        val selected: String? = null,
        val generating: Boolean = false,
        val message: String? = null,
        val messageRes: Int? = null,
        val messageIsError: Boolean = false,
        val info: ReportsInfoResponse? = null,
    )

    private val _uiState = MutableStateFlow(AdvisorUiState())
    val uiState: StateFlow<AdvisorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val info = repo.reportsInfo().getOrNull()
            _uiState.update { it.copy(info = info) }
        }
    }

    fun setRiskAppetite(value: String) {
        _uiState.update { it.copy(riskAppetite = value) }
    }

    fun setHorizon(value: String) {
        _uiState.update { it.copy(horizon = value) }
    }

    fun setProfitability(value: String) {
        _uiState.update { it.copy(profitability = value) }
    }

    fun select(ticker: String) {
        _uiState.update { it.copy(selected = ticker) }
    }

    fun runFit() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(fitting = true, message = null, messageRes = null, messageIsError = false)
            }
            val request = FitRequest(
                horizon = state.horizon,
                profitability = state.profitability,
                riskTolerance = state.riskAppetite,
                limit = 5,
            )
            val result = repo.fitStocks(request)
            _uiState.update { s ->
                result.fold(
                    onSuccess = { s.copy(fitting = false, recommendations = it.results) },
                    onFailure = { t ->
                        val res = ApiErrorMapper.mapApiError(t)
                        s.copy(
                            fitting = false,
                            message = t.message,
                            messageRes = res,
                            messageIsError = true,
                        )
                    },
                )
            }
        }
    }

    fun generate(ticker: String) {
        if (ticker.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    generating = true,
                    selected = ticker,
                    message = null,
                    messageRes = null,
                    messageIsError = false,
                )
            }
            val result = repo.generateReport(ticker.trim().uppercase(), "quick_report")
            _uiState.update { s ->
                result.fold(
                    onSuccess = {
                        s.copy(
                            generating = false,
                            message = "Rapor kuyruğa alındı. Geçmiş sekmesinden takip edebilirsin.",
                            messageRes = null,
                            messageIsError = false,
                        )
                    },
                    onFailure = { t ->
                        val res = ApiErrorMapper.mapApiError(t)
                        s.copy(
                            generating = false,
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