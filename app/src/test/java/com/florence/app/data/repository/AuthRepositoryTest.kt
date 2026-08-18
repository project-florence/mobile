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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: AuthRepository
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
        repo = AuthRepository(api, store)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `login stores tokens`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token":"acc-1","refresh_token":"ref-1","token_type":"bearer"}"""),
        )

        val result = repo.login("burak", "sifre123456")

        assertTrue(result.isSuccess)
        assertEquals("acc-1", store.accessToken)
        assertEquals("ref-1", store.refreshToken)
        // Login is form-encoded (OAuth2PasswordRequestForm), not JSON
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("application/x-www-form-urlencoded", recorded.getHeader("Content-Type"))
        assertTrue(recorded.body.readUtf8().contains("username=burak"))
    }

    @Test
    fun `login failure returns error and keeps store empty`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"Invalid credentials"}"""))

        val result = repo.login("burak", "yanlis-sifre")

        assertTrue(result.isFailure)
        assertFalse(store.session.value)
    }

    @Test
    fun `logout clears store`() = runBlocking {
        store.setTokens("acc", "ref")
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"message":"Logged out"}"""))

        repo.logout()

        assertEquals(null, store.accessToken)
        assertEquals(null, store.refreshToken)
        assertFalse(store.session.value)
    }

    @Test
    fun `register sends expected body`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"message":"Register successful","user_id":42}"""))

        val result = repo.register("burak", "burak@example.com", "uzun-bir-sifre")

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"username\":\"burak\""))
        assertTrue(body.contains("\"email\":\"burak@example.com\""))
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
