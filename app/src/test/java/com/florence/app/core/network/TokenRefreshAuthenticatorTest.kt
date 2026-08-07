package com.florence.app.core.network

import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.AuthEndpoints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Web'deki tek seferlik refresh davranışının OkHttp karşılığı test edilir:
 * 401 → refresh (POST /auth/refresh) → orijinal istek yeni token ile tekrarlanır.
 */
class TokenRefreshAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private val store = FakeTokenStore()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val authApi: AuthEndpoints = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthEndpoints::class.java)

        client = OkHttpClient.Builder()
            .authenticator(TokenRefreshAuthenticator(store, authApi))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `401 triggers refresh and retries with new token`() {
        store.setTokensBlocking("old-access", "old-refresh")

        // 1) orijinal istek → 401   2) refresh → yeni token çifti   3) tekrar deneme → 200
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"Token expired"}"""))
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"access_token":"new-access","refresh_token":"new-refresh"}"""),
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val response = client.newCall(
            Request.Builder().url(server.url("api/v1/bist/tickers")).build(),
        ).execute()

        assertEquals(200, response.code)
        assertEquals("ok", response.body?.string())
        assertEquals("new-access", store.accessToken)

        val requests = server.takeRequests(3)
        assertEquals(3, requests.size)
        // Refresh isteği doğru uca gitti
        assertEquals("/api/v1/auth/refresh", requests[1].path)
        // Tekrarlanan istek yeni access token'ı taşımalı
        assertEquals("Bearer new-access", requests[2].getHeader("Authorization"))
    }

    @Test
    fun `refresh failure clears tokens and does not retry`() {
        store.setTokensBlocking("old-access", "old-refresh")

        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"Token expired"}"""))
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"Invalid refresh token"}"""))

        val response = client.newCall(
            Request.Builder().url(server.url("api/v1/version")).build(),
        ).execute()

        assertEquals(401, response.code)
        assertNull(store.accessToken)
        assertNull(store.refreshToken)
        assertEquals(2, server.takeRequests(2).size)
    }

    @Test
    fun `auth endpoints never trigger refresh`() {
        store.setTokensBlocking("old-access", "old-refresh")

        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"bad"}"""))

        val response = client.newCall(
            Request.Builder().url(server.url("api/v1/auth/login")).build(),
        ).execute()

        assertEquals(401, response.code)
        assertEquals(1, server.takeRequests(1).size) // sadece orijinal istek, refresh yok
    }

    @Test
    fun `concurrent 401s share a single refresh`() {
        store.setTokensBlocking("old-access", "old-refresh")

        // Dispatcher: 401'leri 300ms geciktir ki 4 istek de aynı anda authenticate'e girsin.
        // Refresh anında yanıtlanır; retry (Authorization header'lı) 200 alır.
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path
                return when {
                    path == "/api/v1/auth/refresh" ->
                        MockResponse().setResponseCode(200)
                            .setBody("""{"access_token":"new-access","refresh_token":"new-refresh"}""")

                    path?.startsWith("/api/v1/bist/tickers") == true ->
                        if (request.getHeader("Authorization") != null) {
                            MockResponse().setResponseCode(200).setBody("ok")
                        } else {
                            Thread.sleep(300)
                            MockResponse().setResponseCode(401).setBody("{}")
                        }

                    else -> MockResponse().setResponseCode(200).setBody("ok")
                }
            }
        }

        val threads = (0 until 4).map {
            Thread {
                val response = client.newCall(
                    Request.Builder().url(server.url("api/v1/bist/tickers")).build(),
                ).execute()
                assertTrue("beklenen 200, gelen ${response.code}", response.code == 200)
                response.body?.close()
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals("new-access", store.accessToken)
        val total = server.takeRequests(9)
        // 4 orijinal + 1 refresh + 4 retry = 9; tam olarak 1 refresh olmalı
        val refreshCount = total.count { it.path == "/api/v1/auth/refresh" }
        assertEquals(1, refreshCount)
    }

    private fun MockWebServer.takeRequests(count: Int): List<RecordedRequest> {
        val result = mutableListOf<RecordedRequest>()
        val deadline = System.currentTimeMillis() + 5000
        while (result.size < count && System.currentTimeMillis() < deadline) {
            val request = takeRequest(200, TimeUnit.MILLISECONDS) ?: break
            result += request
        }
        return result
    }

    private class FakeTokenStore : TokenStore {
        override var accessToken: String? = null
        override var refreshToken: String? = null
        private val _session = MutableStateFlow(false)
        override val session: StateFlow<Boolean> = _session.asStateFlow()

        fun setTokensBlocking(access: String, refresh: String) {
            accessToken = access
            refreshToken = refresh
        }

        override suspend fun setTokens(access: String, refresh: String) {
            accessToken = access
            refreshToken = refresh
            _session.value = true
        }

        override suspend fun clear() {
            accessToken = null
            refreshToken = null
            _session.value = false
        }
    }
}
