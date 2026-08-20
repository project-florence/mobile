package com.florence.app.presentation.home

import com.florence.app.core.cache.InMemoryOfflineCache
import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.repository.CompanyRepository
import com.florence.app.data.repository.FavoritesRepository
import com.florence.app.data.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * DashboardViewModel — gerçek repository'ler + MockWebServer + Dispatchers.setMain.
 * viewModelScope varsayılan olarak Dispatchers.Main kullanır; üretimde Android
 * Main looper'ı yokken testte UnconfinedTestDispatcher ile değiştirilir.
 */
class DashboardViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var cache: InMemoryOfflineCache
    private lateinit var marketRepo: MarketRepository
    private lateinit var favoritesRepo: FavoritesRepository
    private lateinit var companyRepo: CompanyRepository

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
        marketRepo = MarketRepository(api, cache, json)
        // Token yok → FavoritesRepository.refresh ağa hiç çıkmaz.
        favoritesRepo = FavoritesRepository(api, NullTokenStore())
        companyRepo = CompanyRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun dashboardDispatcher(
        versionCode: Int = 200,
        companiesCode: Int = 200,
        infoCode: Int = 200,
    ): Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val path = request.path ?: return MockResponse().setResponseCode(404)
            return when {
                path == "/api/v1/version" -> respond(versionCode, """{"version":"1.2.3"}""")
                path == "/api/v1/maintenance" -> respond(200, """{"disabled_features":[]}""")
                path == "/api/v1/market/status" ->
                    respond(200, """{"open":true,"timezone":"Europe/Istanbul","as_of":"2026-08-20T10:00:00Z"}""")
                path.startsWith("/api/v1/bist/companies") ->
                    respond(
                        companiesCode,
                        """[{"ticker":"THYAO","name":"Türk Hava Yolları"},{"ticker":"GARAN","name":"Garanti BBVA"}]""",
                    )
                path.startsWith("/api/v1/companies/info/") ->
                    respond(infoCode, """{"symbol":"X","name":"SomeIso","sector":"Finance"}""")
                path.startsWith("/api/v1/price/history/") ->
                    respond(200, """[{"ts":"2026-08-20T09:55:00Z","close":100.0},{"ts":"2026-08-20T10:00:00Z","close":103.5}]""")
                else -> MockResponse().setResponseCode(404).setBody("not found")
            }
        }
    }

    private fun respond(code: Int, body: String): MockResponse =
        MockResponse().setResponseCode(code).setBody(body)

    @Test
    fun `refresh success loads heroes and companies with no error or stale`() {
        server.dispatcher = dashboardDispatcher()

        val vm = DashboardViewModel(marketRepo, favoritesRepo, companyRepo)

        awaitTrue("uiState yüklenmeli") { !vm.uiState.value.loading }
        val state = vm.uiState.value
        assertFalse("hata olmamalı", state.error)
        assertFalse("stale olmamalı", state.isStale)
        assertEquals("1.2.3", state.version)
        // 6 popüler hisse için info ucu başarılı → heroes dolu.
        assertEquals(POPULAR_TICKERS.size, state.heroes.size)
        assertEquals(2, state.companies.size)
        assertEquals("THYAO", state.companies[0].ticker)
        assertTrue("piyasa açık olmalı", state.marketStatus?.open == true)
        // Sparkline verisi (history ucu) sonradan dolar.
        awaitTrue("sparkline gelmeli") { vm.uiState.value.sparklines.isNotEmpty() }
    }

    @Test
    fun `refresh with version and companies failure surfaces error`() {
        // version + companies 500 → error; kahramanlar yine de dolar.
        server.dispatcher = dashboardDispatcher(versionCode = 500, companiesCode = 500)

        val vm = DashboardViewModel(marketRepo, favoritesRepo, companyRepo)

        awaitTrue("uiState yüklenmeli") { !vm.uiState.value.loading }
        val state = vm.uiState.value
        assertTrue("hata olmalı", state.error)
        assertEquals("version hata → null", null, state.version)
        assertEquals("companies hatası → boş", 0, state.companies.size)
        // Cache boş olduğundan stale fallback yok → isStale false.
        assertFalse("cache'te veri yok → stale değil", state.isStale)
    }

    @Test
    fun `refresh falls back to stale cache marks isStale`() {
        // companies önbelleği TTL dışına itilir → ağ başarısız olunca stale fallback.
        cache.putWithTimestamp(
            MarketRepository.KEY_COMPANIES,
            """[{"ticker":"STALE","name":"Stale Co"}]""",
            MarketRepository.TTL_PRICE_MS,
        )
        cache.advance(MarketRepository.TTL_PRICE_MS + 1)
        // companies ucu 500 döner → stale fallback + isStale bayrağı.
        server.dispatcher = dashboardDispatcher(companiesCode = 500)

        val vm = DashboardViewModel(marketRepo, favoritesRepo, companyRepo)

        awaitTrue("isStale bayrağı set edilmeli") { vm.uiState.value.isStale }
        val state = vm.uiState.value
        assertTrue("stale olmalı", state.isStale)
        assertFalse("stale fallback başarılı → hata olmamalı", state.error)
        assertEquals("stale cache'ten tek şirket", 1, state.companies.size)
        assertEquals("STALE", state.companies[0].ticker)
    }

    private fun awaitTrue(message: String, timeoutMs: Long = 8000, cond: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!cond() && System.currentTimeMillis() < deadline) {
            Thread.sleep(15)
        }
        assertTrue("$message (timeout ${timeoutMs}ms)", cond())
    }

    private class NullTokenStore : TokenStore {
        override var accessToken: String? = null
        override var refreshToken: String? = null
        private val _session = MutableStateFlow(false)
        override val session: StateFlow<Boolean> = _session.asStateFlow()
        private val _vr = MutableStateFlow(false)
        override val verificationRequired: StateFlow<Boolean> = _vr.asStateFlow()
        override fun markVerificationRequired() { _vr.value = true }
        override fun clearVerificationRequired() { _vr.value = false }
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
