package com.florence.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class AuthUiState(
    val busy: Boolean = false,
    val errorRes: Int? = null,
    // E-posta doğrulama akışı
    val verifyPending: Boolean = false,     // true → UI VerifyEmailScreen'i gösterir
    val verifyTarget: String = "",          // resend için username VEYA e-posta
    val verifyAutoResend: Boolean = false,  // register verification_sent=false → otomatik yeniden gönder
    val verifyBusy: Boolean = false,        // resend isteği sürüyor
    val verifyMessageRes: Int? = null,      // bilgilendirme (örn. "mail gönderildi")
    val verifyErrorRes: Int? = null,        // resend hatası (örn. rate-limited)
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.auth_login_error) }
            return
        }
        _uiState.update { it.copy(busy = true, errorRes = null) }
        viewModelScope.launch {
            val result = repo.login(username, password)
            val t = result.exceptionOrNull()
            if (t != null && isEmailNotVerified(t)) {
                // Şifre DOĞRU ama e-posta doğrulanmamış → VerifyEmailScreen'e git.
                _uiState.update {
                    it.copy(
                        busy = false,
                        errorRes = null,
                        verifyPending = true,
                        verifyTarget = username.trim(),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(busy = false, errorRes = t?.let(::mapError))
                }
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        _uiState.update { it.copy(busy = true, errorRes = null) }
        viewModelScope.launch {
            val result = repo.register(username, email, password)
            val t = result.exceptionOrNull()
            if (t == null) {
                val resp = result.getOrNull()
                // Kayıt başarılı: verification_sent ne olursa olsun doğrulama ekranına git.
                _uiState.update {
                    it.copy(
                        busy = false,
                        errorRes = null,
                        verifyPending = true,
                        verifyTarget = email.trim().ifBlank { username.trim() },
                        verifyAutoResend = resp?.verificationSent == false,
                    )
                }
            } else {
                _uiState.update { it.copy(busy = false, errorRes = t.let(::mapError)) }
            }
        }
    }

    /**
     * VerifyEmailScreen'deki "Yeniden gönder" — endpoint 429 (3/saat) dönerse
     * verifyErrorRes rate-limited'a ayarlanır (UI geri sayım gösterir).
     */
    fun resendVerification() {
        val target = _uiState.value.verifyTarget
        if (target.isBlank()) {
            _uiState.update { it.copy(verifyErrorRes = R.string.common_error) }
            return
        }
        _uiState.update { it.copy(verifyBusy = true, verifyErrorRes = null, verifyMessageRes = null) }
        viewModelScope.launch {
            val result = repo.resendVerification(target)
            _uiState.update {
                it.copy(
                    verifyBusy = false,
                    verifyMessageRes = if (result.isSuccess) R.string.verify_sent else null,
                    verifyErrorRes = result.exceptionOrNull()?.let(::mapResendError),
                )
            }
        }
    }

    /** VerifyEmailScreen'den çıkarken akış durumunu temizle. */
    fun dismissVerify() {
        _uiState.update {
            it.copy(
                verifyPending = false,
                verifyAutoResend = false,
                verifyTarget = "",
                verifyBusy = false,
                verifyMessageRes = null,
                verifyErrorRes = null,
            )
        }
        repo.clearVerificationRequired()
    }

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }

    private fun isEmailNotVerified(t: Throwable): Boolean =
        t is HttpException &&
            t.code() == 403 &&
            t.response()?.errorBody()?.string()?.contains("error_email_not_verified") == true

    private fun mapError(t: Throwable): Int = when {
        t is HttpException && t.code() == 401 -> R.string.auth_login_error
        t is HttpException && t.code() == 429 -> R.string.auth_rate_limited
        t is HttpException && t.code() == 400 -> when (httpErrorDetail(t)) {
            "error_email_taken" -> R.string.auth_email_taken
            "error_username_taken" -> R.string.auth_username_taken
            else -> R.string.auth_register_taken
        }
        t is HttpException -> R.string.auth_register_error
        t is IOException -> R.string.auth_network_error
        else -> R.string.common_error
    }

    private fun mapResendError(t: Throwable): Int = when {
        t is HttpException && t.code() == 429 -> R.string.verify_rate_limited
        t is HttpException && t.code() == 404 -> R.string.verify_user_not_found
        t is HttpException && t.code() == 400 -> R.string.verify_already_verified
        t is IOException -> R.string.auth_network_error
        else -> R.string.common_error
    }

    /** backend hata gövdesindeki "detail" alanını döndürür (örn. error_email_taken). */
    private fun httpErrorDetail(t: HttpException): String? =
        t.response()?.errorBody()?.string()?.let { body ->
            runCatching {
                Json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.contentOrNull
            }.getOrNull()
        }
}
