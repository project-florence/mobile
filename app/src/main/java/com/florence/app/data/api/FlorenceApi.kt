package com.florence.app.data.api

import com.florence.app.data.model.AnalyticsEvent
import com.florence.app.data.model.AdminUsersResponse
import com.florence.app.data.model.AnnouncementsResponse
import com.florence.app.data.model.Candle
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.model.CreatePortfolioRequest
import com.florence.app.data.model.CreditTransferRequest
import com.florence.app.data.model.CreditTransferResponse
import com.florence.app.data.model.CreditsResponse
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.FavoritesResponse
import com.florence.app.data.model.GenerateReportRequest
import com.florence.app.data.model.GenerateReportResponse
import com.florence.app.data.model.IpoItem
import com.florence.app.data.model.MaintenanceResponse
import com.florence.app.data.model.NewsItem
import com.florence.app.data.model.Portfolio
import com.florence.app.data.model.PortfolioSnapshot
import com.florence.app.data.model.PortfolioTransaction
import com.florence.app.data.model.AddTransactionRequest
import com.florence.app.data.model.AddTransactionResponse
import com.florence.app.data.model.Quote
import com.florence.app.data.model.ReportDetail
import com.florence.app.data.model.ReportGenerateResponse
import com.florence.app.data.model.ReportHistoryItem
import com.florence.app.data.model.ReportsInfoResponse
import com.florence.app.data.model.Ticker
import com.florence.app.data.model.UserProfile
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
    @GET("api/v1/announcements")
    suspend fun announcements(): AnnouncementsResponse

    // ---- Kullanıcı / Kredi / Admin ----
    @GET("api/v1/profile")
    suspend fun profile(): UserProfile

    @GET("api/v1/credits")
    suspend fun credits(): CreditsResponse

    @GET("api/v1/admin/users")
    suspend fun adminUsers(): AdminUsersResponse

    @POST("api/v1/admin/users/{userId}/credits")
    suspend fun adminTransferCredits(
        @Path("userId") userId: Long,
        @Body body: CreditTransferRequest,
    ): CreditTransferResponse

    // ---- Sanal Portföy ----
    @GET("api/v1/portfolios")
    suspend fun portfolios(): List<Portfolio>

    @POST("api/v1/portfolios")
    suspend fun createPortfolio(@Body body: CreatePortfolioRequest): Portfolio

    @GET("api/v1/portfolios/{id}/snapshot")
    suspend fun portfolioSnapshot(@Path("id") id: String): PortfolioSnapshot

    @GET("api/v1/portfolios/{id}/transactions")
    suspend fun portfolioTransactions(@Path("id") id: String): List<PortfolioTransaction>

    @POST("api/v1/portfolios/{id}/transactions")
    suspend fun addTransaction(
        @Path("id") id: String,
        @Body body: AddTransactionRequest,
    ): AddTransactionResponse

    // ---- IPO ----
    @GET("api/v1/ipos/active")
    suspend fun iposActive(): List<IpoItem>

    // ---- Raporlar ----
    @GET("api/v1/reports/history")
    suspend fun reportsHistory(): List<ReportHistoryItem>

    @GET("api/v1/reports/{reportId}")
    suspend fun reportDetail(@Path("reportId") reportId: Int): ReportDetail

    @POST("api/v1/reports/generate")
    suspend fun generateReport(
        @Query("ticker") ticker: String,
        @Query("type") type: String,
    ): ReportGenerateResponse

    @GET("api/v1/reports/info")
    suspend fun reportsInfo(): ReportsInfoResponse

    @POST("api/v1/reports/generate")
    suspend fun generateReport(@Body body: GenerateReportRequest): GenerateReportResponse

    @POST("api/v1/analytics/event")
    suspend fun trackEvent(@Body event: AnalyticsEvent): Unit
}
