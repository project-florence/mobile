package com.florence.app.data.api

import com.florence.app.data.model.AnalyticsEvent
import com.florence.app.data.model.AuthTokens
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.MaintenanceResponse
import com.florence.app.data.model.Quote
import com.florence.app.data.model.RefreshRequest
import com.florence.app.data.model.RegisterRequest
import com.florence.app.data.model.RegisterResponse
import com.florence.app.data.model.Ticker
import com.florence.app.data.model.VersionResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Auth uçları ayrı bir arayüz — TokenRefreshAuthenticator'ın kullandığı
 * refresh istemcisi yalnızca bunları içerir (döngü riski yok).
 * Backend: POST /api/v1/auth/* — login form-encoded (OAuth2PasswordRequestForm).
 */
interface AuthEndpoints {
    @POST("api/v1/auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): AuthTokens

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthTokens

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshRequest)
}

/**
 * Florence API yüzeyi. Tüm uçlar /api/v1 altındadır (src/api/router.py).
 * v0 kapsamı: auth + sistem + pano (tickers/currency/version/maintenance).
 * Diğer uçlar (portföy, rapor, simülasyon, IPO, haber…) sonraki kilometre taşlarında eklenir.
 */
interface FlorenceApi : AuthEndpoints {

    // ---- Sistem ----
    @GET("api/v1/version")
    suspend fun version(): VersionResponse

    @GET("api/v1/maintenance")
    suspend fun maintenance(): MaintenanceResponse

    // ---- Pano ----
    @GET("api/v1/bist/tickers")
    suspend fun tickers(
        @Query("sort") sort: String = "alphabetical",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): List<Ticker>

    @GET("api/v1/companies/search")
    suspend fun searchCompanies(@Query("query") query: String): List<CompanySearchResult>

    @GET("api/v1/price/current")
    suspend fun priceCurrent(
        @Query("ticker") ticker: String,
        @Query("interval") interval: String = "5m",
    ): Quote

    @GET("api/v1/economy/currency")
    suspend fun currency(@Query("symbols") symbols: String? = null): Map<String, CurrencyQuote>

    // ---- Analitik (web'deki usePageTracking eşleniği) ----
    @POST("api/v1/analytics/event")
    suspend fun trackEvent(@Body event: AnalyticsEvent)
}
