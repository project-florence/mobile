package com.florence.app.core.cache

/**
 * In-memory [OfflineCache] test double. Mirrors the exact timed semantics of the
 * production [SharedPreferencesOfflineCache] contract (fresh-within-TTL vs.
 * stale-fallback) but keeps state in a map and exposes a controllable [now] clock
 * so tests can simulate TTL expiry without any Android Context.
 */
class InMemoryOfflineCache(private val startTime: Long = System.currentTimeMillis()) : OfflineCache {

    /** Test-controlled clock; advance it to simulate time passing. */
    var now: Long = startTime

    private val store = HashMap<String, String>()
    private val expires = HashMap<String, Long>()

    override fun put(key: String, json: String) {
        store[key] = json
        expires.remove(key)
    }

    override fun get(key: String): String? = store[key]

    override fun putWithTimestamp(key: String, json: String, ttlMs: Long) {
        store[key] = json
        expires[key] = now + ttlMs
    }

    override fun getFresh(key: String, ttlMs: Long): String? {
        val json = store[key] ?: return null
        val exp = expires[key] ?: return json // no timestamp → always fresh
        return if (now < exp) json else null
    }

    override fun getStale(key: String): String? = store[key]

    fun advance(millis: Long) {
        now += millis
    }
}
