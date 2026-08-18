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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MarketRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: MarketRepository

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
        repo = MarketRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `tickers returns symbols on success`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""["THYAO","GARAN","SAHOL"]"""),
        )

        val result = repo.tickers(limit = 10)

        assertTrue(result.isSuccess)
        assertEquals(listOf("THYAO", "GARAN", "SAHOL"), result.getOrNull())

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.path!!.contains("sort=alphabetical"))
        assertTrue(recorded.path!!.contains("limit=10"))
    }

    @Test
    fun `tickers returns failure on server error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))

        val result = repo.tickers()

        assertTrue(result.isFailure)
    }

    @Test
    fun `companies returns rich objects on success`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"ticker":"THYAO","name":"Türk Hava Yolları"},{"ticker":"GARAN","name":"Garanti BBVA"}]"""),
        )

        val result = repo.companies(limit = 100)

        assertTrue(result.isSuccess)
        val companies = result.getOrNull()!!
        assertEquals(2, companies.size)
        assertEquals("THYAO", companies[0].ticker)
        assertEquals("Türk Hava Yolları", companies[0].name)
    }

    @Test
    fun `search returns empty result when no matches`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""[]"""))

        val result = repo.search("YOKBOYLE")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
        assertFalse(result.isFailure)
    }
}
