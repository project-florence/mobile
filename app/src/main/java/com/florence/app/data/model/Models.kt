package com.florence.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Auth ----
// Backend sözleşmeleri (src/api/auth.py):
// login: OAuth2PasswordRequestForm (form-encoded username/password)
//        → { access_token, refresh_token, token_type }
// refresh: { refresh_token } → { access_token, refresh_token }
// register: { username, email, password(min 10) } → { message, user_id, verification_sent }
// resend-verification: { username_or_email } → { verification_sent }
// verify-email (public GET): ?token=… → { message }

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
    @SerialName("verification_sent") val verificationSent: Boolean? = null,
)

@Serializable
data class ResendVerificationRequest(
    @SerialName("username_or_email") val usernameOrEmail: String,
)

@Serializable
data class VerificationSentResponse(
    @SerialName("verification_sent") val verificationSent: Boolean = false,
)

@Serializable
data class VerifyEmailResponse(
    val message: String? = null,
    val detail: String? = null,
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

// GET /api/v1/economy/currency  → { "USD": {...}, "EUR": {...} }
@Serializable
data class CurrencyQuote(
    @SerialName("Buying") val buying: String? = null,
    @SerialName("Selling") val selling: String? = null,
    @SerialName("Change") val change: String? = null,
    @SerialName("Type") val type: String? = null,
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
    val type: String,
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

/**
 * /ipos/{slug} detayı — backend halkarz source'tan zengin şema döndürür:
 * {
 *   slug, ticker, company_name,
 *   info: {key→value} (ör. "Halka Arz Fiyatı").
 *   sections: {başlık→paragraf},
 *   company: {city, founded, description},
 *   updated_at
 * }
 */
@Serializable
data class IpoDetail(
    val slug: String? = null,
    val ticker: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    val info: Map<String, String> = emptyMap(),
    val sections: Map<String, String> = emptyMap(),
    val company: IpoCompanyInfo? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class IpoCompanyInfo(
    val city: String? = null,
    val founded: String? = null,
    val description: String? = null,
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

// ---- Duyurular ----
// Backend (src/api/announcements.py): GET /api/v1/announcements → son 7 gün
// {"announcements": [{"id", "title", "content", "sent_by", "created_at",
//                     "updated_at", "is_unread"}, ...]} (auth)
@Serializable
data class AnnouncementItem(
    val id: Long? = null,
    val title: String? = null,
    val content: String? = null,
    @SerialName("sent_by") val sentBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("is_unread") val isUnread: Boolean = false,
)

@Serializable
data class AnnouncementsResponse(
    val announcements: List<AnnouncementItem> = emptyList(),
)

// ---- Makroekonomi ----
// Backend (src/api/macroeconomy.py): GET /api/v1/macroeconomy → tüm alanlar
// float, snake_case. 500 dönebilir (FRED verisi yoksa). Değerler eksik
// olabileceğinden hepsi Double? (nullable).
@Serializable
data class MacroeconomyResponse(
    @SerialName("usa_gdp") val usaGdp: Double? = null,
    @SerialName("usa_real_gdp") val usaRealGdp: Double? = null,
    @SerialName("fed_funds") val fedFunds: Double? = null,
    @SerialName("fed_funds_rate") val fedFundsRate: Double? = null,
    @SerialName("usa_unrate") val usaUnrate: Double? = null,
    @SerialName("brent_crude_oil_price") val brentCrudeOilPrice: Double? = null,
    @SerialName("wti_crude_oil_price") val wtiCrudeOilPrice: Double? = null,
    @SerialName("usa_consumer_cpi") val usaConsumerCpi: Double? = null,
    @SerialName("usa_10y_treasury") val usa10yTreasury: Double? = null,
    @SerialName("dxy") val dxy: Double? = null,
    @SerialName("vix") val vix: Double? = null,
    @SerialName("sp500") val sp500: Double? = null,
    @SerialName("nasdaq") val nasdaq: Double? = null,
    @SerialName("bitcoin") val bitcoin: Double? = null,
)

// ---- Hesap yönetimi (auth) ----
// PUT /api/v1/auth/change-password  →  {current_password, new_password (min 10)}
@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

// PUT /api/v1/auth/change-email  →  {new_email, current_password}
@Serializable
data class ChangeEmailRequest(
    @SerialName("new_email") val newEmail: String,
    @SerialName("current_password") val currentPassword: String,
)

// PUT /api/v1/auth/change-username  →  {new_username, current_password}
@Serializable
data class ChangeUsernameRequest(
    @SerialName("new_username") val newUsername: String,
    @SerialName("current_password") val currentPassword: String,
)

// PUT/DELETE auth uçları → 200 {"message": ...}
@Serializable
data class ChangeResponse(
    val message: String? = null,
)

// ---- Kullanıcı / Kredi (coin) ----
@Serializable
data class UserProfile(
    val username: String? = null,
    val email: String? = null,
    @SerialName("user_type") val userType: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("avatar_id") val avatarId: String? = null,
    val credits: Double? = null,
)

@Serializable
data class CreditsResponse(
    val credits: Double? = null,
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

// ---- Piyasa durumu ----
// GET /api/v1/market/status (public, 60sn Redis cache)
// Yanıt alanları backend'de snake_case döner: next_open_at, is_holiday ...
@Serializable
data class MarketStatusResponse(
    val open: Boolean = false,
    @SerialName("next_open_at") val nextOpenAt: String? = null, // piyasa AÇIKKEN null
    val timezone: String? = null,
    @SerialName("is_holiday") val isHoliday: Boolean = false,
    @SerialName("holiday_name") val holidayName: String? = null,
    @SerialName("as_of") val asOf: String? = null,
)

// ---- Avatar ----
// GET /api/v1/meta/avatars → [{ id, url (göreceli) }, ... 12 kayıt]
@Serializable
data class AvatarItem(
    val id: String? = null,
    val url: String? = null, // GÖRECELİ — app tarafında API origin ile birleştirilir
)

// PUT /api/v1/profile/avatar body → { avatar_id: "avatar-3" }
@Serializable
data class UpdateAvatarRequest(
    @SerialName("avatar_id") val avatarId: String,
)

@Serializable
data class UpdateAvatarResponse(
    val message: String? = null,
    @SerialName("avatar_id") val avatarId: String? = null,
)

// ---- Bot hesapları ----
// POST /api/v1/bots body → { username, password (ops., min 10) }
@Serializable
data class CreateBotRequest(
    val username: String,
    val password: String? = null,
)

// Şifre YALNIZCA bu yanıtta döner (tek seferlik).
@Serializable
data class CreateBotResponse(
    val id: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
)

@Serializable
data class BotItem(
    val id: Long? = null,
    val username: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_login") val lastLogin: String? = null,
)

// GET /api/v1/bots → { bots: [...] }
@Serializable
data class BotsResponse(
    val bots: List<BotItem> = emptyList(),
)

@Serializable
data class DeleteBotResponse(
    val message: String? = null,
)
