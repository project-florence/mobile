package com.florence.app.data.repository

import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.Candle
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.NewsItem
import com.florence.app.data.model.TickerStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Favoriler (takip listesi) — web'deki watchlist eşleniği.
 * Backend: GET/POST/DELETE /api/v1/favorites/{ticker}
 */
@Singleton
class FavoritesRepository @Inject constructor(
    private val api: FlorenceApi,
    private val tokenStore: TokenStore,
) {

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    suspend fun refresh() {
        if (tokenStore.refreshToken == null) {
            _favorites.value = emptySet()
            return
        }
        runCatching { api.favorites() }
            .onSuccess { _favorites.value = it.favorites.toSet() }
    }

    suspend fun toggle(ticker: String) {
        val current = _favorites.value
        val adding = ticker !in current
        // İyimser güncelleme — hata olursa geri al.
        _favorites.value = if (adding) current + ticker else current - ticker
        val result = runCatching {
            if (adding) api.addFavorite(ticker) else api.removeFavorite(ticker)
        }
        if (result.isFailure) _favorites.value = current
    }
}

/**
 * Şirket detayı verileri (info + fiyat geçmişi + haberler).
 */
@Singleton
class CompanyRepository @Inject constructor(
    private val api: FlorenceApi,
) {

    suspend fun info(ticker: String): Result<CompanyInfo> = runCatching { api.companyInfo(ticker) }

    suspend fun history(ticker: String, period: String, interval: String): Result<List<Candle>> =
        runCatching { api.priceHistory(ticker, period, interval) }

    suspend fun news(ticker: String): Result<List<NewsItem>> = runCatching { api.news(ticker) }

    suspend fun stats(ticker: String): Result<TickerStats> = runCatching { api.tickerStats(ticker) }
}
