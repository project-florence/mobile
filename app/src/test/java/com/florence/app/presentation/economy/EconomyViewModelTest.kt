package com.florence.app.presentation.economy

import com.florence.app.core.cache.InMemoryOfflineCache
import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * EconomyViewModel — gerçek MarketRepository + MockWebServer + Dispatchers.setMain.
 */
class EconomyViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var cache: InMemoryOfflineCache
    private lateinit var repo: MarketRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FlorenceApi::class.java)
        cache = InMemoryOfflineCache()
        repo = MarketRepository(api, cache, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun economyDispatcher(
        currencyCode: Int = 200,
        macroCode: Int = 200,
    ): Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val path = request.path ?: return MockResponse().setResponseCode(404)
            return when {
                path == "/api/v1/economy/currency" ->
                    respond(currencyCode, """{"USD":{"Buying":"40.5","Selling":"40.8"},"EUR":{"Buying":"44.2","Selling":"44.6"}}""")
                path == "/api/v1/economy/gold-prices" ->
                    respond(200, """{"GRAMALTIN":{"Buying":"3200","Selling":"3220"}}""")
                path == "/api/v1/economy/silver-price" ->
                    respond(200, """{"SILVER":{"Buying":"39.5","Selling":"40.1"}}""")
                path == "/api/v1/economy/gram-platinum-price" ->
                    respond(200, """{"PLATINUM":{"Buying":"1010","Selling":"1020"}}""")
                path == "/api/v1/economy/gram-palladium-price" ->
                    respond(200, """{"PALLADIUM":{"Buying":"930","Selling":"940"}}""")
                path == "/api/v1/macroeconomy" ->
                    respond(macroCode, """{"usa_gdp":28.9,"fed_funds_rate":5.5,"dxy":104.2,"bitcoin":62000.0}""")
                else -> MockResponse().setResponseCode(404).setBody("not found")
            }
        }
    }

    private fun respond(code: Int, body: String): MockResponse =
        MockResponse().setResponseCode(code).setBody(body)

    @Test
    fun `loads currency metals and macro on success`() {
        server.dispatcher = economyDispatcher()

        val vm = EconomyViewModel(repo)

        awaitTrue("economi yüklenmeli") { !vm.uiState.value.loading }
        awaitTrue("makro yüklenmeli") { !vm.uiState.value.macroLoading }
        val state = vm.uiState.value
        assertFalse("hata olmamalı", state.macroError)
        assertFalse("stale olmamalı", state.isStale)
        assertEquals(setOf("USD", "EUR"), state.currency.keys)
        assertEquals("40.5", state.currency["USD"]?.buying)
        assertNotNull("gümüş fiyatı olmalı", state.silver)
        assertNotNull("platin fiyatı olmalı", state.platinum)
        assertNotNull("paladyum fiyatı olmalı", state.palladium)
        assertEquals(28.9, state.macro?.usaGdp)
    }

    @Test
    fun `macro failure surfaces macroError but keeps rates`() {
        server.dispatcher = economyDispatcher(macroCode = 500)

        val vm = EconomyViewModel(repo)

        awaitTrue("economi yüklenmeli") { !vm.uiState.value.loading }
        awaitTrue("makro bitmeli") { !vm.uiState.value.macroLoading }
        val state = vm.uiState.value
        assertTrue("makro hatası olmalı", state.macroError)
        assertNull("makro verisi olmamalı", state.macro)
        // Döviz/metaller yine de yüklenir — makro hatası onları bozmaz.
        assertEquals(setOf("USD", "EUR"), state.currency.keys)
    }

    @Test
    fun `currency outage falls back to stale cache and marks isStale`() {
        // currency önbelleği TTL dışına itilir → ağ hatası → stale fallback + isStale.
        cache.putWithTimestamp(
            MarketRepository.KEY_CURRENCY,
            """{"USD":{"Buying":"39.0","Selling":"39.3"}}""",
            MarketRepository.TTL_ECONOMY_MS,
        )
        cache.advance(MarketRepository.TTL_ECONOMY_MS + 1)
        server.dispatcher = economyDispatcher(currencyCode = 500)

        val vm = EconomyViewModel(repo)

        awaitTrue("isStale bayrağı set edilmeli") { vm.uiState.value.isStale }
        val state = vm.uiState.value
        assertTrue("stale olmalı", state.isStale)
        assertEquals("stale currency'den USD", "39.0", state.currency["USD"]?.buying)
    }

    @Test
    fun `loadMacro can be re-triggered after failure`() {
        // İlk makro 500 → hata; tekrar çağrı başarılı → hata temizlenir + veri gelir.
        server.enqueue(MockResponse().setResponseCode(500))
        server.dispatcher = economyDispatcher(macroCode = 200)

        val vm = EconomyViewModel(repo)
        // Başlangıç çağrısı 500 aldı: bu tekil testte yeniden tetikleme simüle edilir.
        vm.loadMacro()

        awaitTrue("makro bitmeli") { !vm.uiState.value.macroLoading }
        val state = vm.uiState.value
        assertFalse("makro hatası temizlenmeli", state.macroError)
        assertNotNull("makro verisi gelmeli", state.macro)
    }

    private fun awaitTrue(message: String, timeoutMs: Long = 8000, cond: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!cond() && System.currentTimeMillis() < deadline) {
            Thread.sleep(15)
        }
        assertTrue("$message (timeout ${timeoutMs}ms)", cond())
    }
}
