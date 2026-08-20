package com.florence.app.data.api

import com.florence.app.data.model.AuthTokens
import com.florence.app.data.model.ChangeEmailRequest
import com.florence.app.data.model.ChangePasswordRequest
import com.florence.app.data.model.ChangeResponse
import com.florence.app.data.model.ChangeUsernameRequest
import com.florence.app.data.model.RefreshRequest
import com.florence.app.data.model.RegisterRequest
import com.florence.app.data.model.RegisterResponse
import com.florence.app.data.model.ResendVerificationRequest
import com.florence.app.data.model.VerificationSentResponse
import com.florence.app.data.model.VerifyEmailResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface AuthEndpoints {
    @POST("api/v1/auth/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): AuthTokens

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/v1/auth/resend-verification")
    suspend fun resendVerification(@Body body: ResendVerificationRequest): VerificationSentResponse

    // Public endpoint — no auth token required.
    @GET("api/v1/auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): VerifyEmailResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthTokens

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshRequest)

    // ---- Hesap yönetimi (auth; header'ı AuthInterceptor ekler) ----
    @PUT("api/v1/auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ChangeResponse

    @PUT("api/v1/auth/change-email")
    suspend fun changeEmail(@Body body: ChangeEmailRequest): ChangeResponse

    @PUT("api/v1/auth/change-username")
    suspend fun changeUsername(@Body body: ChangeUsernameRequest): ChangeResponse

    @DELETE("api/v1/auth/delete")
    suspend fun deleteAccount(): ChangeResponse
}
