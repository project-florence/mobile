package com.florence.app.presentation.bots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.data.model.BotItem
import com.florence.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class BotsViewModel @Inject constructor(
    private val user: UserRepository,
) : ViewModel() {

    companion object {
        const val MAX_BOTS = 5
    }

    data class BotsUiState(
        val loading: Boolean = true,
        val bots: List<BotItem> = emptyList(),
        val creating: Boolean = false,
        val deletingId: Long? = null,
        /** Tek seferlik gösterilecek üretilmiş şifre. */
        val createdPassword: String? = null,
        val createdUsername: String? = null,
        /** Oluşturma/silme hata mesajı (string res) — null ise hata yok. */
        val errorRes: Int? = null,
    )

    private val _uiState = MutableStateFlow(BotsUiState())
    val uiState: StateFlow<BotsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorRes = null) }
            val result = user.bots()
            _uiState.update {
                it.copy(
                    loading = false,
                    bots = result.getOrNull() ?: emptyList(),
                )
            }
        }
    }

    fun create(username: String, password: String?) {
        if (_uiState.value.creating) return
        viewModelScope.launch {
            _uiState.update { it.copy(creating = true, errorRes = null) }
            val result = user.createBot(username.trim(), password?.takeIf { it.isNotBlank() })
            if (result.isSuccess) {
                val created = result.getOrNull()
                // Backend şifre ürettiyse (opsiyonel şifre verilmediğinde) tek seferlik göster.
                _uiState.update {
                    it.copy(
                        creating = false,
                        createdPassword = created?.password,
                        createdUsername = created?.username ?: username.trim(),
                        errorRes = null,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(creating = false, errorRes = errorResFor(result.exceptionOrNull()))
                }
            }
            load()
        }
    }

    fun dismissCreatedPassword() {
        _uiState.update { it.copy(createdPassword = null, createdUsername = null) }
    }

    fun delete(bot: BotItem) {
        val id = bot.id ?: return
        if (_uiState.value.deletingId == id) return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingId = id, errorRes = null) }
            val result = user.deleteBot(id.toString())
            _uiState.update { it.copy(deletingId = null) }
            if (result.isFailure) {
                _uiState.update { it.copy(errorRes = R.string.bots_error_generic) }
            }
            load()
        }
    }

    /** Backend hata detayından i18n mesajına eşle. */
    private fun errorResFor(t: Throwable?): Int? {
        if (t is HttpException) {
            val body = try {
                t.response()?.errorBody()?.string()?.lowercase() ?: ""
            } catch (e: Exception) {
                ""
            }
            return when {
                body.contains("bot_limit_reached") -> R.string.bots_error_limit
                body.contains("username_taken") -> R.string.bots_error_username_taken
                body.contains("not_allowed") -> R.string.bots_error_not_allowed
                else -> R.string.bots_error_generic
            }
        }
        return R.string.bots_error_generic
    }
}
