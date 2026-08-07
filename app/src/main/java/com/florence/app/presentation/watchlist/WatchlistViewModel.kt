package com.florence.app.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.repository.FavoritesRepository
import com.florence.app.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val favorites: FavoritesRepository,
    private val market: MarketRepository,
) : ViewModel() {

    data class WatchlistItem(
        val ticker: String,
        val info: CompanyInfo? = null,
    )

    data class WatchlistUiState(
        val loading: Boolean = true,
        val items: List<WatchlistItem> = emptyList(),
    )

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favorites.favorites.collect { favs ->
                if (favs.isEmpty()) {
                    _uiState.value = WatchlistUiState(loading = false, items = emptyList())
                    return@collect
                }
                _uiState.value = WatchlistUiState(loading = true, items = favs.map { WatchlistItem(it) })
                val items = favs.map { fav ->
                    WatchlistItem(fav, market.info(fav).getOrNull())
                }
                _uiState.value = WatchlistUiState(loading = false, items = items)
            }
        }
        viewModelScope.launch { favorites.refresh() }
    }
}
