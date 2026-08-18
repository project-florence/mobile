package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.MaintenanceResponse
import com.florence.app.data.model.MarketStatusResponse
import com.florence.app.data.model.Ticker
import com.florence.app.data.model.VersionResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pano + sistem uçları (misafir modda erişilebilir).
 * Backend: /api/v1/bist/tickers, /api/v1/economy/currency,
 * /api/v1/version, /api/v1/maintenance
 */
@Singleton
class MarketRepository @Inject constructor(
    private val api: FlorenceApi,
) {

    suspend fun version(): Result<VersionResponse> = runCatching { api.version() }

    suspend fun maintenance(): Result<MaintenanceResponse> = runCatching { api.maintenance() }

    suspend fun marketStatus(): Result<MarketStatusResponse> =
        runCatching { api.marketStatus() }

    suspend fun tickers(limit: Int = 50): Result<List<String>> =
        runCatching { api.tickers(sort = "alphabetical", offset = 0, limit = limit) }

    suspend fun companies(limit: Int = 50): Result<List<Ticker>> =
        runCatching { api.companies(sort = "alphabetical", offset = 0, limit = limit) }

    suspend fun search(query: String): Result<List<CompanySearchResult>> =
        runCatching { api.searchCompanies(query) }

    suspend fun info(ticker: String): Result<CompanyInfo> =
        runCatching { api.companyInfo(ticker) }

    suspend fun currency(): Result<Map<String, CurrencyQuote>> =
        runCatching { api.currency(symbols = null) }

    suspend fun goldPrices(): Result<Map<String, CurrencyQuote>> =
        runCatching { api.goldPrices() }

    suspend fun silverPrice(): Result<Map<String, CurrencyQuote>> =
        runCatching { api.silverPrice() }

    suspend fun gramPlatinumPrice(): Result<Map<String, CurrencyQuote>> =
        runCatching { api.gramPlatinumPrice() }

    suspend fun gramPalladiumPrice(): Result<Map<String, CurrencyQuote>> =
        runCatching { api.gramPalladiumPrice() }
}
