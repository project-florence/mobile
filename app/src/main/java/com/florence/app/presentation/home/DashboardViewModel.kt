package com.florence.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.Ticker
import com.florence.app.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val version: String? = null,
    val disabledFeatures: List<String> = emptyList(),
    val currencies: List<Pair<String, CurrencyQuote>> = emptyList(),
    val tickers: List<Ticker> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: MarketRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val version = repo.version()
            val maintenance = repo.maintenance()
            val currency = repo.currency()
            val companies = repo.companies(limit = 50)
            _uiState.update { st ->
                st.copy(
                    loading = false,
                    // Yalnızca tümü başarısızsa hata göster; kısmi veri kabul edilir.
                    error = version.isFailure && currency.isFailure && companies.isFailure,
                    version = version.getOrNull()?.version,
                    disabledFeatures = maintenance.getOrNull()?.disabledFeatures ?: emptyList(),
                    currencies = currency.getOrNull()?.toList() ?: emptyList(),
                    tickers = companies.getOrNull() ?: emptyList(),
                )
            }
        }
    }
}
