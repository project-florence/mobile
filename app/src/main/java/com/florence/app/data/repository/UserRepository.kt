package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.AdminUser
import com.florence.app.data.model.CreditTransferRequest
import com.florence.app.data.model.CreditTransferResponse
import com.florence.app.data.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: FlorenceApi,
) {

    suspend fun profile(): Result<UserProfile> =
        runCatching { api.profile() }

    suspend fun credits(): Result<Double> =
        runCatching { api.credits().credits ?: 0.0 }

    suspend fun adminUsers(): Result<List<AdminUser>> =
        runCatching { api.adminUsers().users }

    suspend fun transferCredits(userId: Long, amount: Double): Result<CreditTransferResponse> =
        runCatching { api.adminTransferCredits(userId, CreditTransferRequest(amount)) }
}
