package com.florence.app.data.api

import com.florence.app.data.model.AuthTokens
import com.florence.app.data.model.RefreshRequest
import com.florence.app.data.model.RegisterRequest
import com.florence.app.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthEndpoints {
    @POST("api/v1/auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): AuthTokens

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthTokens

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshRequest)
}
