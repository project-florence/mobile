package com.florence.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Auth ----
// Backend sözleşmeleri (src/api/auth.py):
// login: OAuth2PasswordRequestForm (form-encoded username/password)
//        → { access_token, refresh_token, token_type }
// refresh: { refresh_token } → { access_token, refresh_token }
// register: { username, email, password(min 10) } → { message, user_id }

@Serializable
data class AuthTokens(
    val access_token: String,
    val refresh_token: String,
    val token_type: String? = null,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class RegisterResponse(
    val message: String = "",
    @SerialName("user_id") val userId: Long? = null,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String? = null,
)

// ---- Sistem ----
@Serializable
data class VersionResponse(val version: String = "")

@Serializable
data class MaintenanceResponse(
    @SerialName("disabled_features") val disabledFeatures: List<String> = emptyList(),
)

// ---- Piyasa ----
@Serializable
data class Ticker(
    val ticker: String? = null,
    val name: String? = null,
    @SerialName("company_name") val companyName: String? = null,
)

@Serializable
data class CompanySearchResult(
    val ticker: String? = null,
    val name: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    val sector: String? = null,
    @SerialName("sub_sector") val subSector: String? = null,
)

// GET /api/v1/price/current?ticker=X  (src/services/quote.py:_build_quote)
@Serializable
data class Quote(
    val ticker: String? = null,
    val price: Double? = null,
    @SerialName("previous_close") val previousClose: Double? = null,
    @SerialName("absolute_change") val absoluteChange: Double? = null,
    @SerialName("change_pct") val changePct: Double? = null,
    @SerialName("as_of") val asOf: String? = null,
    @SerialName("market_status") val marketStatus: String? = null,
    @SerialName("is_stale") val isStale: Boolean? = null,
    @SerialName("change_window") val changeWindow: String? = null,
)

// GET /api/v1/economy/currency  → { "USD": {...}, "EUR": {...} }
@Serializable
data class CurrencyQuote(
    val code: String? = null,
    val name: String? = null,
    val buying: Double? = null,
    val selling: Double? = null,
    val rate: Double? = null,
    val change: Double? = null,
    @SerialName("change_pct") val changePct: Double? = null,
    val time: String? = null,
)

// ---- Analitik ----
@Serializable
data class AnalyticsEvent(
    val event: String,
    @SerialName("user_id") val userId: Long? = null,
    val properties: Map<String, String> = emptyMap(),
)

// ---- Şirket detayı ----
// GET /api/v1/companies/info/{ticker}  (yfinance profil verisi)
@Serializable
data class CompanyInfo(
    val symbol: String? = null,
    val name: String? = null,
    val sector: String? = null,
    val industry: String? = null,
    val currency: String? = null,
    val exchange: String? = null,
    val market: MarketData? = null,
)

@Serializable
data class MarketData(
    @SerialName("currentPrice") val currentPrice: Double? = null,
    @SerialName("previousClose") val previousClose: Double? = null,
    @SerialName("marketCap") val marketCap: Double? = null,
    @SerialName("dayHigh") val dayHigh: Double? = null,
    @SerialName("dayLow") val dayLow: Double? = null,
    @SerialName("regularMarketVolume") val volume: Double? = null,
    @SerialName("fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double? = null,
    @SerialName("fiftyTwoWeekLow") val fiftyTwoWeekLow: Double? = null,
)

// GET /api/v1/price/history/{ticker}  → [{ts, open, high, low, close, volume}]
@Serializable
data class Candle(
    val ts: String? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val volume: Double? = null,
)

// GET /api/v1/news/{ticker}
@Serializable
data class NewsItem(
    val title: String? = null,
    val url: String? = null,
    val source: String? = null,
    val date: String? = null,
    val summary: String? = null,
)

// GET /api/v1/favorites → {"favorites": ["THYAO", ...]}
@Serializable
data class FavoritesResponse(val favorites: List<String> = emptyList())
