package com.florence.app.core.network

import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.AuthEndpoints
import com.florence.app.data.model.RefreshRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Route
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Web'deki tek seferlik (single-flight) refresh akışının OkHttp karşılığı:
 * 401 yanıtında refresh token ile yeni token çifti alınır ve orijinal istek
 * yeni access token ile tekrarlanır. Eşzamanlı 401'ler tek bir refresh çağrısında
 * birleştirilir; refresh başarısızsa tokenlar temizlenir ve login'e düşülür.
 *
 * Backend uyumluluğu:
 * - Refresh ucu: POST /api/v1/auth/refresh  (JSON { refresh_token })
 * - Rate limit: 5 istek/dakika (Redis) → auth yollarında asla özyineleme yok,
 *   refresh başarısızsa döngüye girilmez.
 * - Access token ömrü 1 saat; bu nedenle her oturumda refresh kaçınılmazdır.
 */
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val authApi: AuthEndpoints,
) : Authenticator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var refreshInFlight: Deferred<Boolean>? = null

    override fun authenticate(route: Route?, response: Response): okhttp3.Request? {
        if (response.code != 401) return null

        // Auth uçlarında asla refresh'e girme (login/register/refresh/logout).
        val path = response.request.url.encodedPath
        if (path.contains("/auth/")) return null

        val refreshed = refreshBlocking()
        if (!refreshed) return null

        val token = tokenStore.accessToken ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun refreshBlocking(): Boolean = runBlocking {
        val deferred = mutex.withLock {
            refreshInFlight ?: scope.async { doRefresh() }.also { refreshInFlight = it }
        }
        val ok = deferred.await()
        mutex.withLock {
            if (refreshInFlight === deferred) refreshInFlight = null
        }
        ok
    }

    private suspend fun doRefresh(): Boolean {
        val refreshToken = tokenStore.refreshToken ?: return false
        return try {
            val res = authApi.refresh(RefreshRequest(refreshToken))
            tokenStore.setTokens(res.access_token, res.refresh_token)
            true
        } catch (e: Exception) {
            // 403 detail 'error_email_not_verified': şifre doğru ama e-posta
            // doğrulanmamış — oturumu kapat ve VerifyEmailScreen'e yönlendir.
            if (e is retrofit2.HttpException &&
                e.code() == 403 &&
                e.response()?.errorBody()?.string()?.contains("error_email_not_verified") == true
            ) {
                tokenStore.markVerificationRequired()
            }
            tokenStore.clear()
            false
        }
    }
}
