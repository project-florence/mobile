package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.AvatarItem
import com.florence.app.data.model.BotItem
import com.florence.app.data.model.CreateBotRequest
import com.florence.app.data.model.CreateBotResponse
import com.florence.app.data.model.DeleteBotResponse
import com.florence.app.data.model.UpdateAvatarRequest
import com.florence.app.data.model.UpdateAvatarResponse
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

    // ---- Avatar ----
    suspend fun avatars(): Result<List<AvatarItem>> =
        runCatching { api.avatars() }

    suspend fun updateAvatar(avatarId: String): Result<UpdateAvatarResponse> =
        runCatching { api.updateAvatar(UpdateAvatarRequest(avatarId)) }

    // ---- Bot hesapları ----
    suspend fun createBot(username: String, password: String? = null): Result<CreateBotResponse> =
        runCatching { api.createBot(CreateBotRequest(username, password)) }

    suspend fun bots(): Result<List<BotItem>> =
        runCatching { api.bots().bots }

    suspend fun deleteBot(botId: String): Result<DeleteBotResponse> =
        runCatching { api.deleteBot(botId) }
}
