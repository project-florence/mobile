package com.florence.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token saklama sözleşmesi.
 * Access token kısa ömürlüdür (1 saat) ve yalnızca bellekte tutulur;
 * refresh token günlerce geçerlidir ve EncryptedSharedPreferences'ta şifreli saklanır.
 * Web'deki akışla birebir: access bellek, refresh güvenli depo.
 */
interface TokenStore {
    var accessToken: String?
    val refreshToken: String?
    val session: StateFlow<Boolean>
    /** true → auth UI VerifyEmailScreen'e yönlenmeli (refresh 403 'error_email_not_verified'). */
    val verificationRequired: StateFlow<Boolean>
    suspend fun setTokens(access: String, refresh: String)
    suspend fun clear()
    fun markVerificationRequired()
    fun clearVerificationRequired()
}

@Singleton
class EncryptedTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) : TokenStore {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Volatile
    override var accessToken: String? = null

    override val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)

    private val _session = MutableStateFlow(prefs.contains(KEY_REFRESH))
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
        prefs.edit().putString(KEY_REFRESH, refresh).apply()
        _session.value = true
        _verificationRequired.value = false
    }

    override suspend fun clear() {
        accessToken = null
        prefs.edit().remove(KEY_REFRESH).apply()
        _session.value = false
    }

    private companion object {
        const val FILE_NAME = "florence_secure_store"
        const val KEY_REFRESH = "refresh_token"
    }
}
