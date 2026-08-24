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

// ---- Simülasyon (G1) ----
// Backend (src/api/simulations.py + src/services/simulation_history.py):
// GET /simulations/per-day-cost  → { per_day_cost, round }
// GET /simulations/estimate-cost/{ticker}?days=  → { cost }
// GET /simulations/history?limit=&offset=  → [SimulationHistoryItem]
// GET /simulations/history/{sim_id}  → SimulationDetailResponse (result = JSONB)
// GET /simulations/{ticker}?days=&bounds=&target=  → SimulationResponse (Monte Carlo)
// Simülasyon maliyetlidir (kredi harcar); 402 "insufficient credit" dönebilir.
@Serializable
data class SimulationDailyCostResponse(
    @SerialName("per_day_cost") val perDayCost: Double? = null,
    val round: Int? = null,
)

@Serializable
data class EstimateCostResponse(
    val cost: Double? = null,
)

@Serializable
data class SimulationHistoryItem(
    val id: Int? = null,
    val ticker: String? = null,
    val days: Int? = null,
    val bounds: String? = null,
    val target: String? = null,
    val cost: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// Monte Carlo güven aralığı (bounds -> percent = 1 - 2*bounds).
@Serializable
data class SimulationConfidence(
    val min: Double? = null,
    val max: Double? = null,
    val percent: Double? = null,
    val days: Int? = null,
    val bounds: String? = null,
)

// Simülasyon çalıştırma yanıtı (sonuç + meta bilgi; kredi harcar).
@Serializable
data class SimulationResponse(
    @SerialName("prob_above") val probAbove: Double? = null,
    @SerialName("prob_below") val probBelow: Double? = null,
    val confidence: SimulationConfidence? = null,
    val direction: String? = null,
    @SerialName("simulation_id") val simulationId: Int? = null,
    val ticker: String? = null,
    val days: Int? = null,
    val target: String? = null,
    val bounds: String? = null,
    @SerialName("credits_spend") val creditsSpend: Double? = null,
    @SerialName("remaining_credits") val remainingCredits: Double? = null,
)

// Geçmiş detayı: result alanı kaydedilmiş JSONB (SimulationResponse içeriğiyle aynı şema).
@Serializable
data class SimulationDetailResponse(
    val id: Int? = null,
    val ticker: String? = null,
    val days: Int? = null,
    val bounds: String? = null,
    val target: String? = null,
    val result: SimulationResponse? = null,
    val cost: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// ---- AI Danışman / risk bazlı hisse önerisi (G2) ----
// Backend (src/api/fit.py): POST /api/v1/stocks/fit — advisor feature'ı tarafından
// kapsama alınan risk/horizon/karlılık vektör eşleştirme ucu.
// FitRequest { horizon, profitability, risk_tolerance, limit } → FitResponse.
// risk_tolerance: low | medium | high
// horizon:        short | medium | long
// profitability:  low | medium | high
@Serializable
data class FitRequest(
    val horizon: String,
    val profitability: String,
    @SerialName("risk_tolerance") val riskTolerance: String,
    val limit: Int = 5,
)

@Serializable
data class FitResultItem(
    val ticker: String? = null,
    val vector: List<Double> = emptyList(),
    val score: Double? = null,
    val distance: Double? = null,
)

@Serializable
data class FitResponse(
    val query: Map<String, Double> = emptyMap(),
    val results: List<FitResultItem> = emptyList(),
)

// ---- G3: Portföy analitikleri ----
// Backend (src/api/virtual_portfolio.py + src/services/portfolio.py). Hepsi auth,
// GET /api/v1/portfolios/{portfolio_id}/{valuation|diversification|performers|history|returns|risk|benchmark|performance|stats}.

// /valuation → { total_value, cash_balance, holdings_value, total_pnl, pnl_percentage, assets[] }
// (PortfolioValuation üstte tanımlı; aynı şema.)

// /diversification
@Serializable
data class DiversificationAsset(
    val ticker: String? = null,
    val amount: Double? = null,
    val value: Double? = null,
    val type: String? = null,
    @SerialName("allocation_pct") val allocationPct: Double? = null,
)

@Serializable
data class PortfolioDiversification(
    @SerialName("total_value") val totalValue: Double? = null,
    @SerialName("cash_balance") val cashBalance: Double? = null,
    @SerialName("cash_allocation_pct") val cashAllocationPct: Double? = null,
    val assets: List<DiversificationAsset> = emptyList(),
    @SerialName("allocation_by_type") val allocationByType: Map<String, Double> = emptyMap(),
)

// /performers?top_n → { best[], worst[] }
@Serializable
data class PortfolioPerformer(
    val ticker: String? = null,
    val amount: Double? = null,
    val pnl: Double? = null,
    @SerialName("pnl_percentage") val pnlPercentage: Double? = null,
)

@Serializable
data class PortfolioPerformers(
    val best: List<PortfolioPerformer> = emptyList(),
    val worst: List<PortfolioPerformer> = emptyList(),
)

// /history?period → [ {ts, total_value, cash_balance, holdings_value} ]
@Serializable
data class PortfolioHistoryPoint(
    val ts: String? = null,
    @SerialName("total_value") val totalValue: Double? = null,
    @SerialName("cash_balance") val cashBalance: Double? = null,
    @SerialName("holdings_value") val holdingsValue: Double? = null,
)

// /returns?period
@Serializable
data class PortfolioReturns(
    val period: String? = null,
    @SerialName("start_value") val startValue: Double? = null,
    @SerialName("end_value") val endValue: Double? = null,
    @SerialName("absolute_return") val absoluteReturn: Double? = null,
    @SerialName("total_return_percentage") val totalReturnPercentage: Double? = null,
    @SerialName("cagr_percentage") val cagrPercentage: Double? = null,
)

// /risk?period
@Serializable
data class PortfolioRisk(
    val volatility: Double? = null,
    @SerialName("max_drawdown") val maxDrawdown: Double? = null,
    @SerialName("sharpe_ratio") val sharpeRatio: Double? = null,
)

// /benchmark?ticker
@Serializable
data class BenchmarkComparison(
    @SerialName("portfolio_return_pct") val portfolioReturnPct: Double? = null,
    @SerialName("benchmark_ticker") val benchmarkTicker: String? = null,
    @SerialName("benchmark_return_pct") val benchmarkReturnPct: Double? = null,
    @SerialName("difference_pct") val differencePct: Double? = null,
    val outperformed: Boolean? = null,
)

// /performance → { overall:{}, assets[] }
@Serializable
data class PerformanceOverall(
    @SerialName("efficiency_score") val efficiencyScore: Double? = null,
    @SerialName("actual_pnl") val actualPnl: Double? = null,
    @SerialName("optimal_pnl") val optimalPnl: Double? = null,
)

@Serializable
data class PortfolioPerformance(
    val overall: PerformanceOverall? = null,
    val assets: List<AssetPerformance> = emptyList(),
)

@Serializable
data class AssetPerformance(
    val ticker: String? = null,
    @SerialName("efficiency_score") val efficiencyScore: Double? = null,
    @SerialName("actual_pnl") val actualPnl: Double? = null,
    @SerialName("optimal_pnl") val optimalPnl: Double? = null,
)

// /stats → { total_transactions, total_buys, total_sells, ... }
@Serializable
data class TransactionStats(
    @SerialName("total_transactions") val totalTransactions: Long? = null,
    @SerialName("total_buys") val totalBuys: Long? = null,
    @SerialName("total_sells") val totalSells: Long? = null,
    @SerialName("total_buy_volume") val totalBuyVolume: Double? = null,
    @SerialName("total_sell_volume") val totalSellVolume: Double? = null,
    @SerialName("avg_transaction_size") val avgTransactionSize: Double? = null,
    @SerialName("unique_tickers") val uniqueTickers: Long? = null,
)

// ---- C9: Ek veri uçları ----
// stats/top (public) → [{ ticker, name, info_count, report_count, news_count, history_count, simulation_count, favorite_count, total }]
// stats/{ticker} (public) → { ticker, info_count, ... }
@Serializable
data class TickerStats(
    val ticker: String? = null,
    val name: String? = null,
    @SerialName("info_count") val infoCount: Long? = null,
    @SerialName("report_count") val reportCount: Long? = null,
    @SerialName("news_count") val newsCount: Long? = null,
    @SerialName("history_count") val historyCount: Long? = null,
    @SerialName("simulation_count") val simulationCount: Long? = null,
    @SerialName("favorite_count") val favoriteCount: Long? = null,
    val total: Long? = null,
)

// /companies/summary → { data:[{ticker,name,sector,last_price,change_pct,...}], total }
@Serializable
data class CompanySummary(
    val ticker: String? = null,
    val name: String? = null,
    val sector: String? = null,
    @SerialName("last_price") val lastPrice: Double? = null,
    @SerialName("change_pct") val changePct: Double? = null,
    @SerialName("previous_close") val previousClose: Double? = null,
    @SerialName("absolute_change") val absoluteChange: Double? = null,
    val volume: Double? = null,
    @SerialName("market_cap") val marketCap: Double? = null,
    @SerialName("day_high") val dayHigh: Double? = null,
    @SerialName("day_low") val dayLow: Double? = null,
)

@Serializable
data class CompanySummaryResponse(
    val data: List<CompanySummary> = emptyList(),
    val total: Long? = null,
)

// ---- C10: Kullanıcı tercihleri (backend senkron) ----
// GET  /user/preferences → { prefs JSONB }  (ham nesne döner)
// PUT  /user/preferences → req { prefs: {...} } → senkron + güncel nesne döner.
// Tercihler serbest JSONB olduğundan (lang/theme/…), typed yerine JsonObject kullanılır.
@Serializable
data class UpdatePreferencesRequest(
    val prefs: kotlinx.serialization.json.JsonObject,
)
