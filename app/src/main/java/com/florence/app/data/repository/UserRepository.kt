package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
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
}
