package com.florence.app.data.repository

import com.florence.app.core.cache.OfflineCache
import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.MaintenanceResponse
import com.florence.app.data.model.MacroeconomyResponse
import com.florence.app.data.model.MarketStatusResponse
import com.florence.app.data.model.Ticker
import com.florence.app.data.model.VersionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pano + sistem uçları (misafir modda erişilebilir).
 * Backend: /api/v1/bist/tickers, /api/v1/economy/currency,
 * /api/v1/version, /api/v1/maintenance
 *
 * #D1 — cache-first / offline önbellek
 * Fiyat-odaklı uçlar (companies, info) kısa TTL (60 sn), ekonomi uçları
 * (currency/metals/macroeconomy) uzun TTL (20 dk). Akış:
 *  1. getFresh() — önbellekte taze veri varsa ağa hiç çıkmadan döndür.
 *  2. ağ çağrısı başarılıysa önbelleği yaz + stale bayrağını temizle.
 *  3. ağ hatasıysa getStale() — son bilinen veri varsa onu döndür ve
 *     stale olarak işaretle (offline fallback).
 */
@Singleton
class MarketRepository @Inject constructor(
    private val api: FlorenceApi,
    private val cache: OfflineCache,
    private val json: Json,
) {

    /** Bu TTL'nin dışında kalan (ag sunulamadığı için stale fallback dönen) cache anahtarları. */
    private val _staleKeys = MutableStateFlow<Set<String>>(emptySet())
    val staleKeys: StateFlow<Set<String>> = _staleKeys.asStateFlow()

    suspend fun version(): Result<VersionResponse> = runCatching { api.version() }

    suspend fun maintenance(): Result<MaintenanceResponse> = runCatching { api.maintenance() }

    suspend fun marketStatus(): Result<MarketStatusResponse> =
        runCatching { api.marketStatus() }

    suspend fun tickers(limit: Int = 50): Result<List<String>> =
        runCatching { api.tickers(sort = "alphabetical", offset = 0, limit = limit) }

    suspend fun companies(limit: Int = 50): Result<List<Ticker>> = cacheFirst(
        KEY_COMPANIES,
        TTL_PRICE_MS,
        decodeCache = { s -> decodeList<Ticker>(s) },
        fetch = { api.companies(sort = "alphabetical", offset = 0, limit = limit) },
    )

    suspend fun search(query: String): Result<List<CompanySearchResult>> =
        runCatching { api.searchCompanies(query) }

    suspend fun info(ticker: String): Result<CompanyInfo> = cacheFirst(
        infoKey(ticker),
        TTL_PRICE_MS,
        decodeCache = { s -> decodeSingle<CompanyInfo>(s) },
        fetch = { api.companyInfo(ticker) },
    )

    suspend fun currency(): Result<Map<String, CurrencyQuote>> = cacheFirst(
        KEY_CURRENCY,
        TTL_ECONOMY_MS,
        decodeCache = { s -> decodeSingle<Map<String, CurrencyQuote>>(s) },
        fetch = { api.currency(symbols = null) },
    )

    suspend fun goldPrices(): Result<Map<String, CurrencyQuote>> = cacheFirst(
        KEY_GOLD,
        TTL_ECONOMY_MS,
        decodeCache = { s -> decodeSingle<Map<String, CurrencyQuote>>(s) },
        fetch = { api.goldPrices() },
    )

    suspend fun silverPrice(): Result<Map<String, CurrencyQuote>> = cacheFirst(
        KEY_SILVER,
        TTL_ECONOMY_MS,
        decodeCache = { s -> decodeSingle<Map<String, CurrencyQuote>>(s) },
        fetch = { api.silverPrice() },
    )

    suspend fun gramPlatinumPrice(): Result<Map<String, CurrencyQuote>> = cacheFirst(
        KEY_PLATINUM,
        TTL_ECONOMY_MS,
        decodeCache = { s -> decodeSingle<Map<String, CurrencyQuote>>(s) },
        fetch = { api.gramPlatinumPrice() },
    )

    suspend fun gramPalladiumPrice(): Result<Map<String, CurrencyQuote>> = cacheFirst(
        KEY_PALLADIUM,
        TTL_ECONOMY_MS,
        decodeCache = { s -> decodeSingle<Map<String, CurrencyQuote>>(s) },
        fetch = { api.gramPalladiumPrice() },
    )

    suspend fun macroeconomy(): Result<MacroeconomyResponse> = cacheFirst(
        KEY_MACRO,
        TTL_ECONOMY_MS,
        decodeCache = { s -> decodeSingle<MacroeconomyResponse>(s) },
        fetch = { api.macroeconomy() },
    )

    // ---- cache-first yardımcıları ----

    /**
     * Cache-first döngü: taze cache → ağ → başarılıysa yaz / hata ise stale fallback.
     * Dönüş türü her zaman [Result] olur — imza değişmez, yalnızca iç davranış zenginleşir.
     */
    private suspend inline fun <reified T> cacheFirst(
        key: String,
        ttlMs: Long,
        decodeCache: (String) -> T?,
        fetch: suspend () -> T,
    ): Result<T> {
        cache.getFresh(key, ttlMs)?.let { cached ->
            decodeCache(cached)?.let { return Result.success(it) }
        }
        val result = runCatching { fetch() }
        result.onSuccess { value ->
            try {
                cache.putWithTimestamp(key, json.encodeToString(value), ttlMs)
            } catch (_: Exception) {
                // Cache yazma hatası sonucu bozmaz.
            }
            clearStale(key)
        }
        if (result.isFailure) {
            cache.getStale(key)?.let { stale ->
                decodeCache(stale)?.let { cached ->
                    markStale(key)
                    return Result.success(cached)
                }
            }
        }
        return result
    }

    private inline fun <reified T> decodeList(s: String): List<T>? =
        try { json.decodeFromString<List<T>>(s) } catch (_: Exception) { null }

    private inline fun <reified T> decodeSingle(s: String): T? =
        try { json.decodeFromString<T>(s) } catch (_: Exception) { null }

    private fun markStale(key: String) {
        _staleKeys.update { it + key }
    }

    private fun clearStale(key: String) {
        _staleKeys.update { it - key }
    }

    companion object {
        const val TTL_PRICE_MS = 60_000L
        const val TTL_ECONOMY_MS = 20 * 60_000L

        const val KEY_COMPANIES = "companies.v1"
        const val KEY_CURRENCY = "economy.currency"
        const val KEY_GOLD = "economy.gold"
        const val KEY_SILVER = "economy.silver"
        const val KEY_PLATINUM = "economy.platinum"
        const val KEY_PALLADIUM = "economy.palladium"
        const val KEY_MACRO = "economy.macro"

        private fun infoKey(ticker: String) = "info.$ticker"

        /** Pano ekranı ([DashboardViewModel]) için geçerli stale anahtarları. */
        fun isDashboardStale(keys: Set<String>): Boolean =
            keys.contains(KEY_COMPANIES) || keys.any { it.startsWith("info.") }

        /** Ekonomi ekranı ([EconomyViewModel]) için geçerli stale anahtarları. */
        fun isEconomyStale(keys: Set<String>): Boolean =
            keys.any { it.startsWith("economy.") }
    }
}
