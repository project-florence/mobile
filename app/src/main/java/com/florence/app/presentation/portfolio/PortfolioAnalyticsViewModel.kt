package com.florence.app.presentation.portfolio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.BenchmarkComparison
import com.florence.app.data.model.PortfolioDiversification
import com.florence.app.data.model.PortfolioHistoryPoint
import com.florence.app.data.model.PortfolioPerformance
import com.florence.app.data.model.PortfolioPerformers
import com.florence.app.data.model.PortfolioReturns
import com.florence.app.data.model.PortfolioRisk
import com.florence.app.data.model.PortfolioValuation
import com.florence.app.data.model.TransactionStats
import com.florence.app.data.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioAnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: PortfolioRepository,
) : ViewModel() {

    private val portfolioId: String = savedStateHandle["portfolioId"] ?: ""

    data class AnalyticsUiState(
        val loading: Boolean = true,
        val error: String? = null,
        val valuation: PortfolioValuation? = null,
        val diversification: PortfolioDiversification? = null,
        val performers: PortfolioPerformers? = null,
        val history: List<PortfolioHistoryPoint> = emptyList(),
        val returns: PortfolioReturns? = null,
        val risk: PortfolioRisk? = null,
        val benchmark: BenchmarkComparison? = null,
        val performance: PortfolioPerformance? = null,
        val stats: TransactionStats? = null,
    )

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = coroutineScope {
                val valuation = async { repo.valuation(portfolioId).getOrNull() }
                val diversification = async { repo.diversification(portfolioId).getOrNull() }
                val performers = async { repo.performers(portfolioId, 5).getOrNull() }
                val history = async { repo.history(portfolioId, "1mo").getOrNull().orEmpty() }
                val returns = async { repo.returns(portfolioId, "1mo").getOrNull() }
                val risk = async { repo.risk(portfolioId, "1y").getOrNull() }
                val benchmark = async { repo.benchmark(portfolioId, "XU100").getOrNull() }
                val performance = async { repo.performance(portfolioId).getOrNull() }
                val stats = async { repo.stats(portfolioId).getOrNull() }
                AnalyticsUiState(
                    loading = false,
                    error = null,
                    valuation = valuation.await(),
                    diversification = diversification.await(),
                    performers = performers.await(),
                    history = history.await(),
                    returns = returns.await(),
                    risk = risk.await(),
                    benchmark = benchmark.await(),
                    performance = performance.await(),
                    stats = stats.await(),
                )
            }
            _uiState.update { it.copy(loading = false, error = result.error) }
            if (result.error == null && result.valuation == null) {
                _uiState.update { it.copy(error = "Analitikler yüklenemedi") }
            }
        }
    }
}