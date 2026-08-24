package com.florence.app.core.net

import com.florence.app.R
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

/**
 * Backend hata gövdesini ({ "detail": "error_*" | düz metin }) ve HTTP durum kodunu
 * kullanıcıya gösterilecek i18n string kaynağına eşleyen ortak yardımcı.
 *
 * Bilinen durum kodları: 429 (rate limit), 503 (DB yoğun), 402 (kredi yetersiz).
 * Bilinen "detail" i18n anahtarları: error_email_taken, error_username_taken,
 * error_login_failed, error_email_not_verified, error_bot_limit_reached,
 * error_bots_not_allowed.
 *
 * @return R.string kaynağı; bilinmeyen/yerel olmayan hatalar için [null]
 *         döner — arayan taraf ham exception.message gösterilebilir.
 */
object ApiErrorMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun mapApiError(t: Throwable?): Int? {
        if (t is HttpException) {
            val detail = httpErrorDetail(t)
            return when {
                t.code() == 429 -> R.string.common_rate_limited
                t.code() == 503 -> R.string.common_server_busy
                t.code() == 402 -> R.string.common_insufficient_credit
                t.code() == 403 && detailContains(detail, "error_bots_not_allowed") ->
                    R.string.bots_error_not_allowed
                t.code() == 403 && detailContains(detail, "error_email_not_verified") ->
                    R.string.error_email_not_verified
                t.code() == 409 && detailContains(detail, "error_username_taken") ->
                    R.string.auth_username_taken
                t.code() == 400 && detailContains(detail, "error_bot_limit_reached") ->
                    R.string.bots_error_limit
                t.code() == 400 && detailContains(detail, "error_email_taken") ->
                    R.string.auth_email_taken
                t.code() == 400 && detailContains(detail, "error_username_taken") ->
                    R.string.auth_username_taken
                t.code() == 400 && detailContains(detail, "error_login_failed") ->
                    R.string.auth_login_error
                else -> null
            }
        }
        if (t is IOException) return R.string.common_network_error
        return null
    }

    /** Backend hata gövdesindeki "detail" değerini döndürür; JSON değilse ham gövdeyi döner. */
    fun httpErrorDetail(t: HttpException): String? = runCatching {
        val body = t.response()?.errorBody()?.string() ?: return@runCatching null
        runCatching {
            json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.contentOrNull
        }.getOrNull() ?: body
    }.getOrNull()

    private fun detailContains(detail: String?, needle: String): Boolean =
        detail?.contains(needle, ignoreCase = true) == true
}