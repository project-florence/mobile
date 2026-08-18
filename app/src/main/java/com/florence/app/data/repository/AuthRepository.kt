package com.florence.app.data.repository

import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.RegisterRequest
import com.florence.app.data.model.RegisterResponse
import com.florence.app.data.model.ResendVerificationRequest
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kimlik doğrulama. Web'deki authStore eşleniği:
 * - login form-encoded'dir (backend OAuth2PasswordRequestForm bekler)
 * - access token 1 saat geçerli → her istekte 401 dönerse authenticator yeniler
 * - logout sunucuya refresh token'ı iptal ettirir, sonra yerel temizlik yapar
 * - kayıt sonrası e-posta doğrulaması zorunludur (verification_sent false olabilir)
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: FlorenceApi,
    private val tokenStore: TokenStore,
) {

    val session: StateFlow<Boolean> = tokenStore.session

    /** true olduğunda auth ekranı VerifyEmailScreen'e yönlenmelidir (refresh 403 email_not_verified). */
    val verificationRequired: StateFlow<Boolean> = tokenStore.verificationRequired

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val tokens = api.login(username.trim(), password)
        tokenStore.setTokens(tokens.access_token, tokens.refresh_token)
    }

    suspend fun register(username: String, email: String, password: String): Result<RegisterResponse> = runCatching {
        api.register(RegisterRequest(username.trim(), email.trim(), password))
    }

    suspend fun resendVerification(usernameOrEmail: String): Result<Boolean> = runCatching {
        api.resendVerification(ResendVerificationRequest(usernameOrEmail.trim())).verificationSent
    }

    suspend fun verifyEmail(token: String): Result<Unit> = runCatching {
        api.verifyEmail(token)
    }

    fun markEmailUnverified() {
        tokenStore.markVerificationRequired()
    }

    fun clearVerificationRequired() {
        tokenStore.clearVerificationRequired()
    }

    suspend fun logout(): Result<Unit> = runCatching {
        val refresh = tokenStore.refreshToken
        if (refresh != null) {
            runCatching { api.logout(com.florence.app.data.model.RefreshRequest(refresh)) }
        }
        tokenStore.clear()
    }

    fun isLoggedIn(): Boolean = tokenStore.refreshToken != null
}
