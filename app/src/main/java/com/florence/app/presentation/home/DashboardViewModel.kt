package com.florence.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.Ticker
import com.florence.app.data.repository.FavoritesRepository
import com.florence.app.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val heroes: List<CompanyInfo> = emptyList(),
    val companies: List<Ticker> = emptyList(),
    val favorites: Set<String> = emptySet(),
    /** Hero ticker → 5d/5m kapanış serisi (sparkline için). */
    val sparklines: Map<String, List<Float>> = emptyMap(),
)

/** Panoda öne çıkan hisseler (web'deki popüler widget karşılığı). */
val POPULAR_TICKERS = listOf("THYAO", "ASELS", "GARAN", "AKBNK", "EREGL", "TUPRS")

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: MarketRepository,
    private val favoritesRepo: FavoritesRepository,
    private val companyRepo: com.florence.app.data.repository.CompanyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepo.favorites.collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val version = repo.version()
            val maintenance = repo.maintenance()
            val companies = repo.companies(limit = 50)
            val heroes = coroutineScope {
                POPULAR_TICKERS.map { ticker -> async { repo.info(ticker) } }
                    .mapNotNull { it.await().getOrNull() }
            }
            favoritesRepo.refresh()
            // UI'yi bekleme: hero + hisse listesi gelince pano hemen gösterilir.
            _uiState.update { st ->
                st.copy(
                    loading = false,
                    error = version.isFailure && companies.isFailure,
                    version = version.getOrNull()?.version,
                    disabledFeatures = maintenance.getOrNull()?.disabledFeatures ?: emptyList(),
                    heroes = heroes,
                    companies = companies.getOrNull() ?: emptyList(),
                )
            }
            // Mini grafik (sparkline) verisi arka planda gelir; kartlar sonra güncellenir.
            val sparklines: Map<String, List<Float>> = coroutineScope {
                val map = mutableMapOf<String, List<Float>>()
                POPULAR_TICKERS.forEach { ticker ->
                    map[ticker] = async {
                        val history = companyRepo.history(ticker, period = "5d", interval = "5m")
                        history.getOrNull()?.mapNotNull { c -> c.close?.toFloat() } ?: emptyList<Float>()
                    }.await()
                }
                map
            }
            _uiState.update { it.copy(sparklines = sparklines) }
        }
    }

    fun toggleFavorite(ticker: String) {
        viewModelScope.launch { favoritesRepo.toggle(ticker) }
    }
}
