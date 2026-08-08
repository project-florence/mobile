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

// ---- Sanal Portföy ----
@Serializable
data class PortfolioMetadata(
    val id: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    val name: String? = null,
    @SerialName("initial_balance") val initialBalance: Double? = null,
    val balance: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Portfolio(
    val metadata: PortfolioMetadata? = null,
    val transactions: List<PortfolioTransaction> = emptyList(),
)

@Serializable
data class PortfolioTransaction(
    val id: String? = null,
    val ticker: String? = null,
    val type: String? = null,
    val quantity: Double? = null,
    val price: Double? = null,
    val commission: Double? = null,
    val total: Double? = null,
    val date: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class PortfolioAsset(
    val ticker: String? = null,
    val amount: Double? = null,
    @SerialName("current_price") val currentPrice: Double? = null,
    @SerialName("total_value") val totalValue: Double? = null,
    @SerialName("total_cost") val totalCost: Double? = null,
    @SerialName("weighted_avg_cost") val weightedAvgCost: Double? = null,
    @SerialName("unrealized_pnl") val unrealizedPnl: Double? = null,
    @SerialName("unrealized_pnl_pct") val unrealizedPnlPct: Double? = null,
)

@Serializable
data class PortfolioValuation(
    @SerialName("total_value") val totalValue: Double? = null,
    @SerialName("cash_balance") val cashBalance: Double? = null,
    @SerialName("holdings_value") val holdingsValue: Double? = null,
    @SerialName("total_pnl") val totalPnl: Double? = null,
    @SerialName("pnl_percentage") val pnlPercentage: Double? = null,
    @SerialName("as_of") val asOf: String? = null,
    val assets: List<PortfolioAsset> = emptyList(),
)

@Serializable
data class PortfolioSnapshot(
    val portfolio: PortfolioMetadata? = null,
    val valuation: PortfolioValuation? = null,
)

@Serializable
data class CreatePortfolioRequest(
    val name: String,
    val currency: String = "TRY",
    @SerialName("initial_balance") val initialBalance: Double,
)

@Serializable
data class AddTransactionRequest(
    val ticker: String,
    val type: String = "BUY",
    val quantity: Double,
)

@Serializable
data class AddTransactionResponse(
    val message: String? = null,
)

// ---- IPO ----
@Serializable
data class IpoItem(
    val id: Long? = null,
    val slug: String? = null,
    val title: String? = null,
    val link: String? = null,
    val date: String? = null,
    val status: String? = null,
)

// ---- Raporlar ----
@Serializable
data class ReportHistoryItem(
    val id: Int? = null,
    val ticker: String? = null,
    val type: String? = null,
    val title: String? = null,
    val status: String? = null,
    val cost: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ReportDetail(
    @SerialName("report_id") val reportId: Int? = null,
    val ticker: String? = null,
    val title: String? = null,
    val about: String? = null,
    val date: String? = null,
    val report: String? = null,
    val sentiments: List<ReportSentiment> = emptyList(),
    @SerialName("token_usage") val tokenUsage: Map<String, Int>? = null,
)

@Serializable
data class ReportSentiment(
    val source: String? = null,
    val title: String? = null,
    val sentiment: String? = null,
    val summary: String? = null,
)

@Serializable
data class ReportGenerateResponse(
    @SerialName("report_id") val reportId: Int? = null,
    val status: String? = null,
    val detail: String? = null,
)

@Serializable
data class ReportTypeInfo(
    val type: String? = null,
    @SerialName("name_en") val nameEn: String? = null,
    @SerialName("name_tr") val nameTr: String? = null,
    val description: String? = null,
    val cost: Double? = null,
    @SerialName("credit_cost") val creditCost: Double? = null,
    @SerialName("estimated_seconds") val estimatedSeconds: Int? = null,
)

@Serializable
data class ReportsInfoResponse(
    @SerialName("quick_report") val quickReport: ReportTypeInfo? = null,
    @SerialName("deep_report") val deepReport: ReportTypeInfo? = null,
    @SerialName("simulation") val simulation: ReportTypeInfo? = null,
)

// ---- Hakkımızda / Katkıda Bulunanlar ----
@Serializable
data class AboutResponse(
    val lang: String? = null,
    val content: String? = null,
)

@Serializable
data class Contributor(
    val nickname: String? = null,
    @SerialName("picture_url") val pictureUrl: String? = null,
    @SerialName("github_url") val githubUrl: String? = null,
)

@Serializable
data class ContributorsResponse(
    val contributors: List<Contributor> = emptyList(),
)

// ---- İletişim / Yasal ----
@Serializable
data class ContactResponse(
    val email: String? = null,
    val github: String? = null,
)

@Serializable
data class LegalResponse(
    val policy: String? = null,
    val lang: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    val content: String? = null,
)

@Serializable
data class GenerateReportRequest(
    val ticker: String,
    @SerialName("report_type") val reportType: String = "quick_report",
)

@Serializable
data class GenerateReportResponse(
    @SerialName("report_id") val reportId: String? = null,
    val status: String? = null,
    val message: String? = null,
)

// ---- Duyurular ----
@Serializable
data class AnnouncementItem(
    val id: Long? = null,
    val title: String? = null,
    val body: String? = null,
    val created_at: String? = null,
)

@Serializable
data class AnnouncementsResponse(
    val announcements: List<AnnouncementItem> = emptyList(),
)

// ---- Kullanıcı / Kredi (coin) / Admin ----
@Serializable
data class UserProfile(
    val username: String? = null,
    val email: String? = null,
    @SerialName("user_type") val userType: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val credits: Double? = null,
)

@Serializable
data class CreditsResponse(
    val credits: Double? = null,
)

@Serializable
data class AdminUser(
    val id: Long? = null,
    val username: String? = null,
    val email: String? = null,
    @SerialName("user_type") val userType: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val credits: Double? = null,
)

@Serializable
data class AdminUsersResponse(
    val users: List<AdminUser> = emptyList(),
)

@Serializable
data class CreditTransferRequest(
    val amount: Double,
)

@Serializable
data class CreditTransferResponse(
    @SerialName("user_id") val userId: Long? = null,
    val credits: Double? = null,
    val message: String? = null,
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
