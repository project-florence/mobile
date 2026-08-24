package com.florence.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.R
import com.florence.app.core.net.ApiErrorMapper
import com.florence.app.core.storage.TokenStore
import com.florence.app.data.model.AnnouncementItem
import com.florence.app.data.model.AvatarItem
import com.florence.app.data.repository.AuthRepository
import com.florence.app.data.repository.MiscRepository
import com.florence.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.HttpException
import javax.inject.Inject

/** Profil ekranında çalıştırılabilecek hesap işlemleri (hangi dialog'da spinner gösterileceğini seçer). */
enum class AccountAction {
    CHANGE_PASSWORD,
    CHANGE_EMAIL,
    CHANGE_USERNAME,
    DELETE_ACCOUNT,
    EXPORT,
}

data class AccountUiState(
    val busyAction: AccountAction? = null,
    val errorRes: Int? = null,
    val successRes: Int? = null,
    val exported: String? = null,
    val exportErrorRes: Int? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val misc: MiscRepository,
    private val tokenStore: TokenStore,
    private val auth: AuthRepository,
    private val user: UserRepository,
) : ViewModel() {

    /** 2-boşluk girintili pretty-print JSON üretir. */
    @OptIn(ExperimentalSerializationApi::class)
    private val prettyJson: Json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    data class ProfileUiState(
        val loading: Boolean = true,
        val announcements: List<AnnouncementItem> = emptyList(),
        /** avatar_id → göreceli `/avatars/…` URL haritası (profil başlığındaki gerçek SVG için). */
        val avatarUrls: Map<String, String> = emptyMap(),
        val loggedIn: Boolean = true,
        val loggingOut: Boolean = false,
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _account = MutableStateFlow(AccountUiState())
    val account: StateFlow<AccountUiState> = _account.asStateFlow()

    init {
        viewModelScope.launch {
            // Duyuruları ve avatar listesini paralel yükle (ikisi de bağımsız GET).
            val announcementsDeferred = async { misc.announcements() }
            val avatarsDeferred = async { user.avatars() }
            val announcements = announcementsDeferred.await().getOrNull() ?: emptyList()
            val avatarUrls = avatarsDeferred.await()
                .getOrNull()
                .orEmpty()
                .mapNotNull { avatar ->
                    avatar.id?.let { id -> avatar.url?.let { url -> id to url } }
                }
                .toMap()
            _uiState.update {
                it.copy(
                    loading = false,
                    announcements = announcements,
                    avatarUrls = avatarUrls,
                )
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loggingOut = true) }
            tokenStore.clear()
            _uiState.update { it.copy(loggingOut = false, loggedIn = false) }
            onDone()
        }
    }

    // ---- Hesap yönetimi ----

    fun changePassword(currentPassword: String, newPassword: String) {
        _account.update { it.copy(busyAction = AccountAction.CHANGE_PASSWORD, errorRes = null) }
        viewModelScope.launch {
            runAction(AccountAction.CHANGE_PASSWORD, successRes = R.string.account_success_password) {
                auth.changePassword(currentPassword, newPassword)
            }
        }
    }

    fun changeEmail(newEmail: String, currentPassword: String) {
        _account.update { it.copy(busyAction = AccountAction.CHANGE_EMAIL, errorRes = null) }
        viewModelScope.launch {
            runAction(AccountAction.CHANGE_EMAIL, successRes = R.string.account_success_email) {
                auth.changeEmail(newEmail.trim(), currentPassword)
            }
        }
    }

    fun changeUsername(newUsername: String, currentPassword: String, onChanged: () -> Unit) {
        _account.update { it.copy(busyAction = AccountAction.CHANGE_USERNAME, errorRes = null) }
        viewModelScope.launch {
            val result = auth.changeUsername(newUsername.trim(), currentPassword)
            val success = result.isSuccess
            _account.update { it.copy(busyAction = null, successRes = if (success) R.string.account_success_username else null, errorRes = result.exceptionOrNull()?.let(::mapAccountError)) }
            if (success) onChanged()
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        _account.update { it.copy(busyAction = AccountAction.DELETE_ACCOUNT, errorRes = null) }
        viewModelScope.launch {
            val result = auth.deleteAccount()
            if (result.isSuccess) {
                // Hesap silindi → oturumu kapat. Session StateFlow login ekranına yönlendirir.
                auth.logout()
                onDeleted()
            } else {
                _account.update { it.copy(busyAction = null, errorRes = result.exceptionOrNull()?.let(::mapAccountError)) }
            }
        }
    }

    // ---- Veri dışa aktarımı (C5) ----

    fun exportData() {
        if (_account.value.busyAction != null) return
        _account.update { it.copy(busyAction = AccountAction.EXPORT, exportErrorRes = null) }
        viewModelScope.launch {
            val result = user.userExport()
            _account.update {
                it.copy(
                    busyAction = null,
                    exported = result.getOrNull()?.let(::prettyPrint),
                    exportErrorRes = result.exceptionOrNull()?.let(::mapAccountError),
                )
            }
        }
    }

    fun dismissExport() {
        _account.update { it.copy(exported = null, exportErrorRes = null) }
    }

    /** Sonuç mesajını (başarı/hata) tükettikten sonra temizler. */
    fun clearAccountResult() {
        _account.update { it.copy(successRes = null, errorRes = null) }
    }

    private suspend fun runAction(action: AccountAction, successRes: Int, block: suspend () -> Result<Unit>) {
        val result = block()
        _account.update {
            it.copy(
                busyAction = null,
                successRes = if (result.isSuccess) successRes else null,
                errorRes = result.exceptionOrNull()?.let(::mapAccountError),
            )
        }
    }

    private fun prettyPrint(json: JsonObject): String =
        prettyJson.encodeToString(JsonElement.serializer(), json)

    private fun mapAccountError(t: Throwable): Int {
        // Mevcut şifre hatalı (401) — hesap yönetimine özel mesaj.
        if (t is HttpException && t.code() == 401) return R.string.account_current_password_incorrect
        // Bilinen durum kodları ve error_* i18n anahtarlarını ortak mapper'a delege et.
        return ApiErrorMapper.mapApiError(t) ?: R.string.common_error
    }
}
