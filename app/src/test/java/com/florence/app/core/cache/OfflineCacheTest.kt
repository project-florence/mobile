package com.florence.app.core.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * #D1 — OfflineCache TTL kontratı. In-memory double, üretim
 * SharedPreferencesOfflineCache ile aynı zaman/taze/stale semantiğini uygular.
 */
class OfflineCacheTest {

    private lateinit var cache: InMemoryOfflineCache

    @Before
    fun setUp() {
        cache = InMemoryOfflineCache(startTime = 1_000L)
    }

    @Test
    fun `put and get round-trip ignores freshness`() {
        cache.put("k", "v")
        assertEquals("v", cache.get("k"))
        assertEquals("v", cache.getStale("k"))
    }

    @Test
    fun `getFresh returns value within ttl and null after expiry`() {
        cache.putWithTimestamp("k", "v", ttlMs = 60_000)
        assertEquals("v", cache.getFresh("k", 60_000))
        cache.advance(60_001)
        assertNull(cache.getFresh("k", 60_000))
    }

    @Test
    fun `getStale still returns value after ttl expired`() {
        cache.putWithTimestamp("k", "v", ttlMs = 60_000)
        cache.advance(120_000)
        assertNull(cache.getFresh("k", 60_000))
        assertEquals("v", cache.getStale("k"))
    }

    @Test
    fun `put without timestamp is treated as always fresh`() {
        cache.put("k", "v")
        cache.advance(1_000_000)
        assertEquals("v", cache.getFresh("k", 60_000))
    }

    @Test
    fun `missing key returns null`() {
        assertNull(cache.get("nope"))
        assertNull(cache.getFresh("nope", 60_000))
        assertNull(cache.getStale("nope"))
    }
}
