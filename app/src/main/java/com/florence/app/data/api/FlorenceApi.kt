package com.florence.app.data.api

import com.florence.app.data.model.AnalyticsEvent
import com.florence.app.data.model.AnnouncementsResponse
import com.florence.app.data.model.Candle
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.model.CreatePortfolioRequest
import com.florence.app.data.model.CreditsResponse
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.FavoritesResponse
import com.florence.app.data.model.IpoDetail
import com.florence.app.data.model.IpoItem
import com.florence.app.data.model.MaintenanceResponse
import com.florence.app.data.model.MarketStatusResponse
import com.florence.app.data.model.NewsItem
import com.florence.app.data.model.Portfolio
import com.florence.app.data.model.PortfolioSnapshot
import com.florence.app.data.model.PortfolioTransaction
import com.florence.app.data.model.AddTransactionRequest
import com.florence.app.data.model.AddTransactionResponse
import com.florence.app.data.model.AboutResponse
import com.florence.app.data.model.AvatarItem
import com.florence.app.data.model.BotsResponse
import com.florence.app.data.model.ContactResponse
import com.florence.app.data.model.CreateBotRequest
import com.florence.app.data.model.CreateBotResponse
import com.florence.app.data.model.DeleteBotResponse
import com.florence.app.data.model.LegalResponse
import com.florence.app.data.model.MacroeconomyResponse
import com.florence.app.data.model.ReportDetail
import com.florence.app.data.model.ReportGenerateResponse
import com.florence.app.data.model.ReportHistoryItem
import com.florence.app.data.model.ContributorsResponse
import com.florence.app.data.model.ReportsInfoResponse
import com.florence.app.data.model.Ticker
import com.florence.app.data.model.UpdateAvatarRequest
import com.florence.app.data.model.UpdateAvatarResponse
import com.florence.app.data.model.UserProfile
import com.florence.app.data.model.VersionResponse
import com.florence.app.data.model.SimulationDailyCostResponse
import com.florence.app.data.model.EstimateCostResponse
import com.florence.app.data.model.SimulationHistoryItem
import com.florence.app.data.model.SimulationDetailResponse
import com.florence.app.data.model.SimulationResponse
import com.florence.app.data.model.FitRequest
import com.florence.app.data.model.FitResponse
import com.florence.app.data.model.PortfolioValuation
import com.florence.app.data.model.PortfolioDiversification
import com.florence.app.data.model.PortfolioPerformers
import com.florence.app.data.model.PortfolioHistoryPoint
import com.florence.app.data.model.PortfolioReturns
import com.florence.app.data.model.PortfolioRisk
import com.florence.app.data.model.BenchmarkComparison
import com.florence.app.data.model.PortfolioPerformance
import com.florence.app.data.model.TransactionStats
import com.florence.app.data.model.TickerStats
import com.florence.app.data.model.CompanySummaryResponse
import com.florence.app.data.model.UpdatePreferencesRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.json.JsonObject

/**
 * Florence API yüzeyi. Tüm uçlar /api/v1 altındadır (src/api/router.py).
 * Kapsam: auth, sistem, pano, ekonomi (döviz/metaller), favoriler,
 * haberler, sanal portföy, IPO, raporlar, analitik ve profil/kredi uçları.
 */
interface FlorenceApi : AuthEndpoints {

    // ---- Sistem ----
    @GET("api/v1/version")
    suspend fun version(): VersionResponse

    @GET("api/v1/maintenance")
    suspend fun maintenance(): MaintenanceResponse

    // ---- Piyasa durumu (public, 60sn Redis cache) ----
    @GET("api/v1/market/status")
    suspend fun marketStatus(): MarketStatusResponse

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

    // ---- Metaller (altın türleri, gümüş, platin, paladyum) ----
    @GET("api/v1/economy/gold-prices")
    suspend fun goldPrices(): Map<String, CurrencyQuote>

    @GET("api/v1/economy/silver-price")
    suspend fun silverPrice(): Map<String, CurrencyQuote>

    @GET("api/v1/economy/gram-platinum-price")
    suspend fun gramPlatinumPrice(): Map<String, CurrencyQuote>

    @GET("api/v1/economy/gram-palladium-price")
    suspend fun gramPalladiumPrice(): Map<String, CurrencyQuote>

    // ---- Makroekonomi (500 dönebilir: FRED verisi yoksa) ----
    @GET("api/v1/macroeconomy")
    suspend fun macroeconomy(): MacroeconomyResponse

    // ---- Kullanıcı veri dışa aktarımı (auth) ----
    // Karmaşık JSON; tam model parse etmek zahmetli olduğundan JsonObject olarak döner.
    @GET("api/v1/user/export")
    suspend fun userExport(): JsonObject

    // ---- Analitik (web'deki usePageTracking eşleniği) ----
    @GET("api/v1/announcements")
    suspend fun announcements(): AnnouncementsResponse

    // ---- Hakkımızda / Katkıda Bulunanlar ----
    @GET("api/v1/about")
    suspend fun about(@Query("lang") lang: String): AboutResponse

    @GET("api/v1/contributors")
    suspend fun contributors(): ContributorsResponse

    // ---- İletişim / Yasal ----
    @GET("api/v1/contact")
    suspend fun contact(): ContactResponse

    @GET("api/v1/legal")
    suspend fun legal(
        @Query("policy") policy: String,
        @Query("lang") lang: String,
    ): LegalResponse

    // ---- Kullanıcı / Kredi ----
    @GET("api/v1/profile")
    suspend fun profile(): UserProfile

    // Avatarlar (public): göreceli url döner → app tarafında API origin ile birleştirilir.
    @GET("api/v1/meta/avatars")
    suspend fun avatars(): List<AvatarItem>

    @PUT("api/v1/profile/avatar")
    suspend fun updateAvatar(@Body body: UpdateAvatarRequest): UpdateAvatarResponse

    @GET("api/v1/credits")
    suspend fun credits(): CreditsResponse

    // ---- Bot hesapları (auth, normal kullanıcı) ----
    @POST("api/v1/bots")
    suspend fun createBot(@Body body: CreateBotRequest): CreateBotResponse

    @GET("api/v1/bots")
    suspend fun bots(): BotsResponse

    @DELETE("api/v1/bots/{botId}")
    suspend fun deleteBot(@Path("botId") botId: String): DeleteBotResponse

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

        // ---- G3: Portföy analitikleri (auth) ----
        @GET("api/v1/portfolios/{id}/valuation")
        suspend fun portfolioValuation(@Path("id") id: String): PortfolioValuation

        @GET("api/v1/portfolios/{id}/diversification")
        suspend fun portfolioDiversification(@Path("id") id: String): PortfolioDiversification

        @GET("api/v1/portfolios/{id}/performers")
        suspend fun portfolioPerformers(@Path("id") id: String, @Query("top_n") topN: Int = 5): PortfolioPerformers

        @GET("api/v1/portfolios/{id}/history")
        suspend fun portfolioHistory(@Path("id") id: String, @Query("period") period: String = "1mo"): List<PortfolioHistoryPoint>

        @GET("api/v1/portfolios/{id}/returns")
        suspend fun portfolioReturns(@Path("id") id: String, @Query("period") period: String = "1mo"): PortfolioReturns

        @GET("api/v1/portfolios/{id}/risk")
        suspend fun portfolioRisk(@Path("id") id: String, @Query("period") period: String = "1y"): PortfolioRisk

        @GET("api/v1/portfolios/{id}/benchmark")
        suspend fun portfolioBenchmark(@Path("id") id: String, @Query("ticker") ticker: String = "XU100"): BenchmarkComparison

        @GET("api/v1/portfolios/{id}/performance")
        suspend fun portfolioPerformance(@Path("id") id: String): PortfolioPerformance

        @GET("api/v1/portfolios/{id}/stats")
        suspend fun portfolioStats(@Path("id") id: String): TransactionStats

        // ---- C9: Ek veri uçları (public) ----
        @GET("api/v1/stats/top")
        suspend fun statsTop(@Query("limit") limit: Int = 50): List<TickerStats>

        @GET("api/v1/stats/{ticker}")
        suspend fun tickerStats(@Path("ticker") ticker: String): TickerStats

        @GET("api/v1/companies/summary")
        suspend fun companiesSummary(
            @Query("limit") limit: Int = 50,
            @Query("offset") offset: Int = 0,
            @Query("sort") sort: String = "popular",
        ): CompanySummaryResponse

        // ---- C10: Kullanıcı tercihleri (auth) ----
        // GET /user/preferences ham JSONB nesnesi döner; PUT req { prefs: {...} } → güncel nesne.
        @GET("api/v1/user/preferences")
        suspend fun userPreferences(): JsonObject

        @PUT("api/v1/user/preferences")
        suspend fun updateUserPreferences(@Body body: UpdatePreferencesRequest): JsonObject

        // ---- IPO ---- (dört uç da public, auth yok)
        @GET("api/v1/ipos/upcoming")
        suspend fun upcomingIpos(): List<IpoItem>

        @GET("api/v1/ipos/active")
        suspend fun iposActive(): List<IpoItem>

        @GET("api/v1/ipos/draft")
        suspend fun draftIpos(): List<IpoItem>

        @GET("api/v1/ipos/{slug}")
        suspend fun ipoDetail(@Path("slug") slug: String): IpoDetail

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

        // ---- Simülasyon (G1) ----
        // Monte Carlo simülasyonu; simulate maliyetlidir (kredi harcar).
        @GET("api/v1/simulations/per-day-cost")
        suspend fun simulationDailyCost(): SimulationDailyCostResponse

        @GET("api/v1/simulations/estimate-cost/{ticker}")
        suspend fun estimateSimulationCost(
            @Path("ticker") ticker: String,
            @Query("days") days: Int,
        ): EstimateCostResponse

        @GET("api/v1/simulations/history")
        suspend fun simulationHistory(
            @Query("limit") limit: Int = 20,
            @Query("offset") offset: Int = 0,
        ): List<SimulationHistoryItem>

        @GET("api/v1/simulations/history/{simId}")
        suspend fun simulationDetail(@Path("simId") simId: Int): SimulationDetailResponse

        @GET("api/v1/simulations/{ticker}")
        suspend fun simulate(
            @Path("ticker") ticker: String,
            @Query("days") days: Int,
            @Query("bounds") bounds: String = "0.05",
            @Query("target") target: String? = null,
        ): SimulationResponse

        // ---- AI Danışman / risk bazlı öneri (G2) ----
        @POST("api/v1/stocks/fit")
        suspend fun fitStocks(@Body body: FitRequest): FitResponse

        @POST("api/v1/analytics/event")
    suspend fun trackEvent(@Body event: AnalyticsEvent): Unit
}
