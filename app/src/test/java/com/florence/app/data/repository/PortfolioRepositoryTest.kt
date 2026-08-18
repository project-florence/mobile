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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PortfolioRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: PortfolioRepository

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
        repo = PortfolioRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `portfolios returns list on success`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """[
                        {"metadata":{"id":"p1","name":"Ana Portföy","initial_balance":100000.0,"balance":120000.0}},
                        {"metadata":{"id":"p2","name":"Hisse","initial_balance":50000.0,"balance":40000.0}}
                    ]""",
                ),
        )

        val result = repo.portfolios()

        assertTrue(result.isSuccess)
        val portfolios = result.getOrNull()!!
        assertEquals(2, portfolios.size)
        assertEquals("p1", portfolios[0].metadata?.id)
        assertEquals("Ana Portföy", portfolios[0].metadata?.name)
    }

    @Test
    fun `portfolios returns failure on server error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"detail":"boom"}"""))

        val result = repo.portfolios()

        assertTrue(result.isFailure)
    }

    @Test
    fun `snapshot returns valuation on success`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{
                        "portfolio":{"id":"p1","name":"Ana Portföy"},
                        "valuation":{"total_value":120000.0,"cash_balance":20000.0,"total_pnl":20000.0}
                    }""",
                ),
        )

        val result = repo.snapshot("p1")

        assertTrue(result.isSuccess)
        val snapshot = result.getOrNull()!!
        assertNotNull(snapshot.valuation)
        assertEquals(120000.0, snapshot.valuation?.totalValue)
    }

    @Test
    fun `addTransaction posts request and succeeds`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"message":"Transaction added"}"""),
        )

        val result = repo.addTransaction("p1", "THYAO", "BUY", 25.0)

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.contains("/portfolios/p1/transactions"))
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"ticker\":\"THYAO\""))
        assertTrue(body.contains("\"type\":\"BUY\""))
        assertTrue(body.contains("\"quantity\":25.0"))
    }

    @Test
    fun `addTransaction returns failure on server error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"detail":"Invalid quantity"}"""))

        val result = repo.addTransaction("p1", "THYAO", "BUY", -5.0)

        assertTrue(result.isFailure)
    }
}
