package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class UserRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: UserRepository

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
        repo = UserRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `profile returns user on success`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"username":"burak","email":"burak@example.com","user_type":"premium","credits":120.0}""",
                ),
        )

        val result = repo.profile()

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals("burak", profile.username)
        assertEquals("burak@example.com", profile.email)
        assertEquals(120.0, profile.credits)

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path!!.contains("/profile"))
    }

    @Test
    fun `profile returns failure on server error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"Not authenticated"}"""))

        val result = repo.profile()

        assertTrue(result.isFailure)
    }

    @Test
    fun `credits returns credit balance`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"credits":45.0}"""))

        val result = repo.credits()

        assertTrue(result.isSuccess)
        assertEquals(45.0, result.getOrNull())
    }

    @Test
    fun `credits defaults to zero when field missing`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{}"""))

        val result = repo.credits()

        assertTrue(result.isSuccess)
        assertEquals(0.0, result.getOrNull())
    }
}
