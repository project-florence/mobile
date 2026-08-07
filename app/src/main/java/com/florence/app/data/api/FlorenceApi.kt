package com.florence.app.data.api

import com.florence.app.data.model.AnalyticsEvent
import com.florence.app.data.model.Candle
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.FavoritesResponse
import com.florence.app.data.model.MaintenanceResponse
import com.florence.app.data.model.NewsItem
import com.florence.app.data.model.Quote
import com.florence.app.data.model.Ticker
import com.florence.app.data.model.VersionResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    // NOT: /bist/tickers düz string dizisi döner (["THYAO", ...]).
    @GET("api/v1/bist/tickers")
    suspend fun tickers(
        @Query("sort") sort: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): List<String>

    // /bist/companies zengin nesneler döner ({ticker, name, city, ...}).
    @GET("api/v1/bist/companies")
    suspend fun companies(
        @Query("sort") sort: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): List<Ticker>

    @GET("api/v1/companies/search")
    suspend fun searchCompanies(@Query("query") query: String): List<CompanySearchResult>

    @GET("api/v1/price/current")
    suspend fun priceCurrent(
        @Query("ticker") ticker: String,
        @Query("interval") interval: String,
    ): Quote

    @GET("api/v1/companies/info/{ticker}")
    suspend fun companyInfo(@Path("ticker") ticker: String): CompanyInfo

    @GET("api/v1/price/history/{ticker}")
    suspend fun priceHistory(
        @Path("ticker") ticker: String,
        @Query("period") period: String,
        @Query("interval") interval: String,
    ): List<Candle>

    @GET("api/v1/news/{ticker}")
    suspend fun news(@Path("ticker") ticker: String): List<NewsItem>

    // ---- Favoriler ----
    @GET("api/v1/favorites")
    suspend fun favorites(): FavoritesResponse

    @POST("api/v1/favorites/{ticker}")
    suspend fun addFavorite(@Path("ticker") ticker: String)

    @DELETE("api/v1/favorites/{ticker}")
    suspend fun removeFavorite(@Path("ticker") ticker: String)

    @GET("api/v1/economy/currency")
    suspend fun currency(@Query("symbols") symbols: String?): Map<String, CurrencyQuote>

    // ---- Analitik (web'deki usePageTracking eşleniği) ----
    @POST("api/v1/analytics/event")
    suspend fun trackEvent(@Body event: AnalyticsEvent)
}
