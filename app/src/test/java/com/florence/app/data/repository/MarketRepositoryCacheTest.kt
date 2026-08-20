package com.florence.app.data.repository

import com.florence.app.core.cache.InMemoryOfflineCache
import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.Ticker
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * #D1 — MarketRepository cache-first / offline önbellek davranışı.
 * Gerçek repository + gerçek MockWebServer + in-memory cache double kullanır.
 */
class MarketRepositoryCacheTest {

    private lateinit var server: MockWebServer
    private lateinit var cache: InMemoryOfflineCache
    private lateinit var repo: MarketRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        cache = InMemoryOfflineCache()
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FlorenceApi::class.java)
        repo = MarketRepository(api, cache, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fresh cache is served without hitting the network again`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"ticker":"THYAO","name":"Turk Hava"}]"""),
        )
        val first = repo.companies(limit = 50)
        assertTrue(first.isSuccess)
        assertEquals(1, server.requestCount)

        // TTL içindeyken ikinci çağrı ağa çıkmamalı.
        val second = repo.companies(limit = 50)
        assertTrue(second.isSuccess)
        assertEquals(1, server.requestCount)
        assertEquals("THYAO", second.getOrNull()!![0].ticker)
        assertFalse(repo.staleKeys.value.contains(MarketRepository.KEY_COMPANIES))
    }

    @Test
    fun `network failure falls back to stale cache and marks it stale`() = runBlocking {
        val body = """[{"ticker":"GARAN","name":"Garanti"}]"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val first = repo.companies(limit = 50)
        assertTrue(first.isSuccess)
        val cached: Ticker = first.getOrNull()!![0]

        // TTL'yi geç → getFresh null olur, repo tekrar ağa çıkar.
        cache.advance(MarketRepository.TTL_PRICE_MS + 1_000)
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))

        val second = repo.companies(limit = 50)
        assertTrue("stale fallback hâlâ success dönmeli", second.isSuccess)
        assertEquals(cached.ticker, second.getOrNull()!![0].ticker)
        assertEquals(2, server.requestCount)
        // Offline fallback kullanıldığı için anahtar stale işaretlenmeli.
        assertTrue(repo.staleKeys.value.contains(MarketRepository.KEY_COMPANIES))
    }

    @Test
    fun `failure without any cache returns failure`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))
        val result = repo.companies(limit = 50)
        assertTrue(result.isFailure)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `fresh refresh clears the stale flag`() = runBlocking {
        // Önce offline senaryo: stale döndür.
        server.enqueue(MockResponse().setResponseCode(200).setBody("""[{"ticker":"A"}]"""))
        repo.companies(limit = 50)
        cache.advance(MarketRepository.TTL_PRICE_MS + 1_000)
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))
        repo.companies(limit = 50)
        assertTrue(repo.staleKeys.value.contains(MarketRepository.KEY_COMPANIES))

        // Başarılı ağ çağrısı geldiğinde stale bayrağı temizlenmeli.
        server.enqueue(MockResponse().setResponseCode(200).setBody("""[{"ticker":"B"}]"""))
        val result = repo.companies(limit = 50)
        assertTrue(result.isSuccess)
        assertFalse(repo.staleKeys.value.contains(MarketRepository.KEY_COMPANIES))
    }
}
