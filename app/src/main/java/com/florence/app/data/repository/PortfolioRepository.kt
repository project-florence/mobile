package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.CreatePortfolioRequest
import com.florence.app.data.model.IpoItem
import com.florence.app.data.model.Portfolio
import com.florence.app.data.model.PortfolioSnapshot
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
}

@Singleton
class MiscRepository @Inject constructor(private val api: FlorenceApi) {

    suspend fun ipos(): Result<List<IpoItem>> =
        runCatching { api.iposActive() }

    suspend fun announcements(): Result<List<com.florence.app.data.model.AnnouncementItem>> =
        runCatching { api.announcements().announcements }

    suspend fun reportsHistory(): Result<List<ReportHistoryItem>> =
        runCatching { api.reportsHistory() }

    suspend fun reportsInfo(): Result<ReportsInfoResponse> =
        runCatching { api.reportsInfo() }

    suspend fun generateReport(ticker: String, reportType: String): Result<Unit> =
        runCatching {
            api.generateReport(
                com.florence.app.data.model.GenerateReportRequest(
                    ticker = ticker,
                    reportType = reportType,
                )
            )
        }
}
