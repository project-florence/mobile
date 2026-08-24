package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.AddTransactionRequest
import com.florence.app.data.model.BenchmarkComparison
import com.florence.app.data.model.CreatePortfolioRequest
import com.florence.app.data.model.CompanySummaryResponse
import com.florence.app.data.model.Portfolio
import com.florence.app.data.model.PortfolioDiversification
import com.florence.app.data.model.PortfolioHistoryPoint
import com.florence.app.data.model.PortfolioPerformance
import com.florence.app.data.model.PortfolioPerformers
import com.florence.app.data.model.PortfolioReturns
import com.florence.app.data.model.PortfolioRisk
import com.florence.app.data.model.PortfolioSnapshot
import com.florence.app.data.model.PortfolioTransaction
import com.florence.app.data.model.PortfolioValuation
import com.florence.app.data.model.TickerStats
import com.florence.app.data.model.TransactionStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepository @Inject constructor(private val api: FlorenceApi) {

    suspend fun portfolios(): Result<List<Portfolio>> =
        runCatching { api.portfolios() }

    suspend fun createPortfolio(name: String, initialBalance: Double): Result<Portfolio> =
        runCatching {
            api.createPortfolio(CreatePortfolioRequest(name = name, initialBalance = initialBalance))
        }

    suspend fun snapshot(portfolioId: String): Result<PortfolioSnapshot> =
        runCatching { api.portfolioSnapshot(portfolioId) }

    suspend fun transactions(portfolioId: String): Result<List<PortfolioTransaction>> =
        runCatching { api.portfolioTransactions(portfolioId) }

    suspend fun addTransaction(
        portfolioId: String,
        ticker: String,
        type: String,
        quantity: Double,
    ): Result<Unit> =
        runCatching {
            api.addTransaction(
                portfolioId,
                AddTransactionRequest(ticker = ticker, type = type, quantity = quantity),
            )
        }

    // ---- G3: Portföy analitikleri (auth) ----
    suspend fun valuation(portfolioId: String): Result<PortfolioValuation> =
        runCatching { api.portfolioValuation(portfolioId) }

    suspend fun diversification(portfolioId: String): Result<PortfolioDiversification> =
        runCatching { api.portfolioDiversification(portfolioId) }

    suspend fun performers(portfolioId: String, topN: Int = 5): Result<PortfolioPerformers> =
        runCatching { api.portfolioPerformers(portfolioId, topN) }

    suspend fun history(portfolioId: String, period: String = "1mo"): Result<List<PortfolioHistoryPoint>> =
        runCatching { api.portfolioHistory(portfolioId, period) }

    suspend fun returns(portfolioId: String, period: String = "1mo"): Result<PortfolioReturns> =
        runCatching { api.portfolioReturns(portfolioId, period) }

    suspend fun risk(portfolioId: String, period: String = "1y"): Result<PortfolioRisk> =
        runCatching { api.portfolioRisk(portfolioId, period) }

    suspend fun benchmark(portfolioId: String, ticker: String = "XU100"): Result<BenchmarkComparison> =
        runCatching { api.portfolioBenchmark(portfolioId, ticker) }

    suspend fun performance(portfolioId: String): Result<PortfolioPerformance> =
        runCatching { api.portfolioPerformance(portfolioId) }

    suspend fun stats(portfolioId: String): Result<TransactionStats> =
        runCatching { api.portfolioStats(portfolioId) }

    // ---- C9: Ek veri uçları (public) ----
    suspend fun topStats(limit: Int = 50): Result<List<TickerStats>> =
        runCatching { api.statsTop(limit) }

    suspend fun tickerStats(ticker: String): Result<TickerStats> =
        runCatching { api.tickerStats(ticker) }

    suspend fun companiesSummary(limit: Int = 50): Result<CompanySummaryResponse> =
        runCatching { api.companiesSummary(limit = limit, offset = 0, sort = "popular") }
}