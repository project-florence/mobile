package com.florence.app.data.repository

import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.FlorenceApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class FavoritesRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: FavoritesRepository
    private val store = FakeTokenStore()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FlorenceApi::class.java)
        repo = FavoritesRepository(api, store)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `refresh loads favorites from backend`() = runBlocking {
        store.refreshToken = "ref-1"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"favorites":["THYAO","GARAN"]}"""),
        )

        repo.refresh()

        assertEquals(setOf("THYAO", "GARAN"), repo.favorites.value)
        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path!!.contains("/favorites"))
    }

    @Test
    fun `refresh clears favorites without calling backend when logged out`() = runBlocking {
        store.refreshToken = null

        repo.refresh()

        // No network call should be made when logged out, and state is empty.
        assertTrue(repo.favorites.value.isEmpty())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `toggle adds ticker optimistically and keeps it on success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{}"""))

        repo.toggle("THYAO")

        assertTrue(repo.favorites.value.contains("THYAO"))
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.contains("/favorites/THYAO"))
    }

    @Test
    fun `toggle reverts on failure`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))

        repo.toggle("THYAO")

        // Optimistic add rolled back because the backend call failed.
        assertFalse(repo.favorites.value.contains("THYAO"))
        assertTrue(repo.favorites.value.isEmpty())
    }

    @Test
    fun `toggle removes ticker on success and reverts on failure`() = runBlocking {
        // Seed favorites via a successful refresh first.
        store.refreshToken = "ref-1"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"favorites":["THYAO","GARAN"]}"""),
        )
        repo.refresh()
        assertEquals(setOf("THYAO", "GARAN"), repo.favorites.value)

        // Consume the refresh GET request so toggle's request is next in queue.
        val refreshRequest = server.takeRequest()
        assertEquals("GET", refreshRequest.method)

        // Remove succeeds -> stays removed.
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{}"""))
        repo.toggle("THYAO")
        assertEquals(setOf("GARAN"), repo.favorites.value)
        val removeRequest = server.takeRequest()
        assertEquals("DELETE", removeRequest.method)
        assertTrue(removeRequest.path!!.contains("/favorites/THYAO"))

        // Remove fails -> reverts to pre-call state ({GARAN}, since THYAO was already removed above).
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))
        repo.toggle("GARAN")
        assertEquals(setOf("GARAN"), repo.favorites.value)
    }

    private class FakeTokenStore : TokenStore {
        override var accessToken: String? = null
        override var refreshToken: String? = null
        private val _session = MutableStateFlow(false)
        override val session: StateFlow<Boolean> = _session.asStateFlow()
        private val _verificationRequired = MutableStateFlow(false)
        override val verificationRequired: StateFlow<Boolean> = _verificationRequired.asStateFlow()

        override fun markVerificationRequired() {
            _verificationRequired.value = true
        }

        override fun clearVerificationRequired() {
            _verificationRequired.value = false
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
