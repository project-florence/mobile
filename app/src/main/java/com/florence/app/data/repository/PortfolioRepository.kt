package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.AddTransactionRequest
import com.florence.app.data.model.AboutResponse
import com.florence.app.data.model.ContactResponse
import com.florence.app.data.model.Contributor
import com.florence.app.data.model.LegalResponse
import com.florence.app.data.model.CreatePortfolioRequest
import com.florence.app.data.model.IpoItem
import com.florence.app.data.model.Portfolio
import com.florence.app.data.model.PortfolioSnapshot
import com.florence.app.data.model.PortfolioTransaction
import com.florence.app.data.model.ReportDetail
import com.florence.app.data.model.ReportGenerateResponse
import com.florence.app.data.model.ReportHistoryItem
import com.florence.app.data.model.ReportsInfoResponse
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
}

@Singleton
class MiscRepository @Inject constructor(private val api: FlorenceApi) {

    suspend fun ipos(): Result<List<IpoItem>> =
        runCatching { api.iposActive() }

    suspend fun announcements(): Result<List<com.florence.app.data.model.AnnouncementItem>> =
        runCatching { api.announcements().announcements }

    suspend fun reportsHistory(): Result<List<ReportHistoryItem>> =
        runCatching { api.reportsHistory() }

    suspend fun reportDetail(reportId: Int): Result<ReportDetail> =
        runCatching { api.reportDetail(reportId) }

    suspend fun generateReport(ticker: String, type: String): Result<ReportGenerateResponse> =
        runCatching { api.generateReport(ticker, type) }

    suspend fun reportsInfo(): Result<ReportsInfoResponse> =
        runCatching { api.reportsInfo() }

    suspend fun about(lang: String): Result<AboutResponse> =
        runCatching { api.about(lang) }

    suspend fun contributors(): Result<List<Contributor>> =
        runCatching { api.contributors().contributors }

    suspend fun contact(): Result<ContactResponse> =
        runCatching { api.contact() }

    suspend fun legal(policy: String, lang: String): Result<LegalResponse> =
        runCatching { api.legal(policy, lang) }
}
