package com.florence.app.core.network

import com.florence.app.core.storage.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Web'deki axios istek interceptor'ünün (Bearer token ekleme) OkHttp karşılığı.
 * Token yoksa istek olduğu gibi geçer — 401 gelirse TokenRefreshAuthenticator devreye girer.
 */
class AuthInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
