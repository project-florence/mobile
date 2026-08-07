package com.florence.app.presentation.company

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.data.model.Candle
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.NewsItem
import com.florence.app.data.repository.CompanyRepository
import com.florence.app.data.repository.FavoritesRepository
import com.florence.app.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RangeOption(
    val period: String,
    val interval: String,
    val labelRes: Int,
)

val CHART_RANGES = listOf(
    RangeOption("5d", "5m", R.string.range_5m),
    RangeOption("1mo", "30m", R.string.range_30m),
    RangeOption("1mo", "1h", R.string.range_1h),
    RangeOption("6mo", "1d", R.string.range_6mo),
    RangeOption("1y", "1d", R.string.range_1y),
)

data class CompanyDetailUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val info: CompanyInfo? = null,
    val candles: List<Candle> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val newsError: Boolean = false,
    // Varsayılan: 1Ay/30dk — cron 5m+30m mumları doldurur; 1d aralığı isteğe bağlı gelir.
    val range: RangeOption = CHART_RANGES[1],
    val isFavorite: Boolean = false,
)

@HiltViewModel
class CompanyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val marketRepo: MarketRepository,
    private val companyRepo: CompanyRepository,
    private val favoritesRepo: FavoritesRepository,
) : ViewModel() {

    val ticker: String = checkNotNull(savedStateHandle["ticker"])

    private val _uiState = MutableStateFlow(CompanyDetailUiState())
    val uiState: StateFlow<CompanyDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepo.favorites.collect { favs ->
                _uiState.update { it.copy(isFavorite = ticker in favs) }
            }
        }
        loadInfo()
        loadNews()
        loadHistory(CHART_RANGES[1])
    }

    fun loadInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            val info = marketRepo.info(ticker)
            _uiState.update {
                it.copy(loading = false, error = info.isFailure, info = info.getOrNull())
            }
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            val news = companyRepo.news(ticker)
            _uiState.update {
                it.copy(news = news.getOrNull() ?: emptyList(), newsError = news.isFailure)
            }
        }
    }

    fun selectRange(range: RangeOption) {
        if (_uiState.value.range == range) return
        _uiState.update { it.copy(range = range) }
        loadHistory(range)
    }

    private fun loadHistory(range: RangeOption) {
        viewModelScope.launch {
            val history = companyRepo.history(ticker, range.period, range.interval)
            _uiState.update { it.copy(candles = history.getOrNull() ?: emptyList()) }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch { favoritesRepo.toggle(ticker) }
    }
}
