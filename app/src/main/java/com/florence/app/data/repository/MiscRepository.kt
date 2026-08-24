package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.AboutResponse
import com.florence.app.data.model.AnnouncementItem
import com.florence.app.data.model.ContactResponse
import com.florence.app.data.model.Contributor
import com.florence.app.data.model.IpoDetail
import com.florence.app.data.model.IpoItem
import com.florence.app.data.model.LegalResponse
import com.florence.app.data.model.ReportDetail
import com.florence.app.data.model.ReportGenerateResponse
import com.florence.app.data.model.ReportHistoryItem
import com.florence.app.data.model.ReportsInfoResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiscRepository @Inject constructor(private val api: FlorenceApi) {

    suspend fun ipos(): Result<List<IpoItem>> =
        runCatching { api.iposActive() }

    suspend fun upcomingIpos(): Result<List<IpoItem>> =
        runCatching { api.upcomingIpos() }

    suspend fun draftIpos(): Result<List<IpoItem>> =
        runCatching { api.draftIpos() }

    suspend fun ipoDetail(slug: String): Result<IpoDetail> =
        runCatching { api.ipoDetail(slug) }

    suspend fun announcements(): Result<List<AnnouncementItem>> =
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