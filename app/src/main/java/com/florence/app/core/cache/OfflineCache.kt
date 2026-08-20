package com.florence.app.core.cache

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal, timestamp-based offline cache used by repositories to serve the last
 * successful server response while offline or during a network outage.
 *
 * Deliberately built on SharedPreferences (no DataStore dependency) — the same
 * storage family as [com.florence.app.core.settings.SettingsRepository]. Entries
 * are stored as raw JSON strings, keyed per resource, with an optional per-key
 * expiry timestamp for TTL-based freshness.
 */
interface OfflineCache {

    /** Store [json] under [key] with no expiry (treat as always-fresh). */
    fun put(key: String, json: String)

    /** Read whatever is stored under [key], ignoring freshness. */
    fun get(key: String): String?

    /** Store [json] under [key] with an expiry of [ttlMs] from now. */
    fun putWithTimestamp(key: String, json: String, ttlMs: Long)

    /** Return the cached value only if it was written within [ttlMs]; else null. */
    fun getFresh(key: String, ttlMs: Long): String?

    /** Return the cached value even if stale/expired (offline fallback); else null. */
    fun getStale(key: String): String?
}

/**
 * SharedPreferences-backed [OfflineCache]. Thread-safe enough for the small,
 * single-writer workloads the repositories perform (each write commits atomically
 * through a SharedPreferences edit).
 */
@Singleton
class SharedPreferencesOfflineCache @Inject constructor(
    @ApplicationContext context: Context,
) : OfflineCache {

    private val prefs =
        context.getSharedPreferences("florence_cache", Context.MODE_PRIVATE)

    override fun put(key: String, json: String) {
        prefs.edit()
            .putString(key, json)
            .remove(expiryKey(key))
            .apply()
    }

    override fun get(key: String): String? = prefs.getString(key, null)

    override fun putWithTimestamp(key: String, json: String, ttlMs: Long) {
        prefs.edit()
            .putString(key, json)
            .putLong(expiryKey(key), System.currentTimeMillis() + ttlMs)
            .apply()
    }

    override fun getFresh(key: String, ttlMs: Long): String? {
        val json = get(key) ?: return null
        val expires = prefs.getLong(expiryKey(key), NO_EXPIRY)
        // No timestamp recorded via put() → treat as fresh forever.
        if (expires == NO_EXPIRY) return json
        return if (System.currentTimeMillis() < expires) json else null
    }

    override fun getStale(key: String): String? = get(key)

    private fun expiryKey(key: String) = "$key.__expires"

    private companion object {
        const val NO_EXPIRY = Long.MIN_VALUE
    }
}
