package com.florence.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.florence.app.BuildConfig
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import com.florence.app.presentation.components.AvatarArt
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.clickableNoRipple
import com.florence.app.presentation.settings.CreditsViewModel
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Saate göre selamlama anahtarı: 05-11 Günaydın, 12-17 İyi günler, 18-22 İyi akşamlar, 23-04 İyi geceler. */
fun greetingResFor(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..17 -> R.string.greeting_day
    in 18..22 -> R.string.greeting_evening
    else -> R.string.greeting_night
}

/** Hangi hesap dialog'unun açık olduğunu tutar. */
private enum class AccountDialog { PASSWORD, EMAIL, USERNAME, DELETE }

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onOpenAvatar: () -> Unit = {},
    onOpenBots: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    creditsViewModel: CreditsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val creditsState by creditsViewModel.uiState.collectAsStateWithLifecycle()
    val accountState by viewModel.account.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var activeDialog by remember { mutableStateOf<AccountDialog?>(null) }

    // Değişken form alanları (tek dialog açık olduğundan tek set yeterli).
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
    var fieldErrorRes by remember { mutableStateOf<Int?>(null) }

    // Başarı mesajı → Toast + dialog'u kapat.
    LaunchedEffect(accountState.successRes) {
        accountState.successRes?.let {
            Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show()
            viewModel.clearAccountResult()
            currentPassword = ""
            newPassword = ""
            newEmail = ""
            newUsername = ""
            fieldErrorRes = null
            activeDialog = null
        }
    }
    // Hata mesajı dialog içinde göster.
    LaunchedEffect(accountState.errorRes) {
        accountState.errorRes?.let { fieldErrorRes = it }
    }

    val profile = creditsState.profile
    val username = profile?.username ?: "Yatırımcı"

    // Canlı saat — her 30 saniyede bir güncellenir.
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(30_000)
        }
    }
    val greeting = stringResource(greetingResFor(now.hour), username)
    val clock = now.format(DateTimeFormatter.ofPattern("HH:mm"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ---- Selamlama başlığı (avatar + kimlik) ----
        item {
            ProfileHeader(
                username = username,
                email = profile?.email,
                isAdmin = creditsState.isAdmin,
                greeting = greeting,
                clock = clock,
                avatarId = profile?.avatarId,
                onAvatarClick = onOpenAvatar,
            )
        }

        // ---- Bots girişi ----
        item {
            FlorenceCard(onClick = onOpenBots, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = null,
                        tint = FlorencePalettes.Florence.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.size(14.dp))
                    Text(
                        text = stringResource(R.string.nav_bots),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                }
            }
        }

        // ---- Coin kartı ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        FlorencePalettes.Florence.primary,
                                        FlorencePalettes.Florence.secondary,
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Coin Bakiyesi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Raporlar ve simülasyonlar coin harcar",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                    Text(
                        text = creditsState.credits?.let { "%.1f".format(it) } ?: "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    IconButton(onClick = { creditsViewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                    }
                }
            }
        }

        // ---- Hesap Yönetimi (C4) ----
        item {
            Text(
                text = stringResource(R.string.account_management),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    AccountActionRow(
                        icon = Icons.Filled.Lock,
                        label = stringResource(R.string.account_change_password),
                        onClick = { activeDialog = AccountDialog.PASSWORD },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AccountActionRow(
                        icon = Icons.Filled.Email,
                        label = stringResource(R.string.account_change_email),
                        onClick = { activeDialog = AccountDialog.EMAIL },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AccountActionRow(
                        icon = Icons.Filled.Person,
                        label = stringResource(R.string.account_change_username),
                        onClick = { activeDialog = AccountDialog.USERNAME },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AccountActionRow(
                        icon = Icons.Filled.Share,
                        label = stringResource(R.string.account_export),
                        onClick = { viewModel.exportData() },
                    )
                }
            }
        }
        // ---- Tehlikeli: hesap silme (C4) ----
        item {
            OutlinedButton(
                onClick = { activeDialog = AccountDialog.DELETE },
                enabled = accountState.busyAction == null || accountState.busyAction == AccountAction.DELETE_ACCOUNT,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DownColor),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.account_delete))
            }
        }

        item {
            Text(
                text = stringResource(R.string.profile_announcements),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (uiState.loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (uiState.announcements.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.profile_no_announcements),
                    subtitle = stringResource(R.string.profile_no_announcements_hint),
                )
            }
        } else {
            items(uiState.announcements, key = { it.id ?: it.hashCode() }) { ann ->
                FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = FlorencePalettes.Florence.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = ann.title ?: "Duyuru",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        ann.content?.let {
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        ann.createdAt?.let {
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = it.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.size(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Florence v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.size(8.dp))
            OutlinedButton(
                onClick = { viewModel.logout(onLoggedOut) },
                enabled = !uiState.loggingOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DownColor),
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(if (uiState.loggingOut) stringResource(R.string.profile_logging_out) else stringResource(R.string.profile_logout))
            }
        }
    }

    // ---- Hesap dialog'ları ----
    when (activeDialog) {
        AccountDialog.PASSWORD -> ChangePasswordDialog(
            currentPassword = currentPassword,
            newPassword = newPassword,
            onCurrentChange = { currentPassword = it; fieldErrorRes = null },
            onNewChange = { newPassword = it; fieldErrorRes = null },
            errorRes = fieldErrorRes,
            busy = accountState.busyAction == AccountAction.CHANGE_PASSWORD,
            onDismiss = { activeDialog = null; fieldErrorRes = null },
            onConfirm = {
                val v = validatePasswordChange(currentPassword, newPassword)
                if (v != null) fieldErrorRes = v
                else viewModel.changePassword(currentPassword, newPassword)
            },
        )
        AccountDialog.EMAIL -> ChangeEmailDialog(
            newEmail = newEmail,
            currentPassword = currentPassword,
            onEmailChange = { newEmail = it; fieldErrorRes = null },
            onPasswordChange = { currentPassword = it; fieldErrorRes = null },
            errorRes = fieldErrorRes,
            busy = accountState.busyAction == AccountAction.CHANGE_EMAIL,
            onDismiss = { activeDialog = null; fieldErrorRes = null },
            onConfirm = {
                if (newEmail.isBlank() || currentPassword.isBlank()) fieldErrorRes = R.string.common_error
                else viewModel.changeEmail(newEmail, currentPassword)
            },
        )
        AccountDialog.USERNAME -> ChangeUsernameDialog(
            newUsername = newUsername,
            currentPassword = currentPassword,
            onUsernameChange = { newUsername = it; fieldErrorRes = null },
            onPasswordChange = { currentPassword = it; fieldErrorRes = null },
            errorRes = fieldErrorRes,
            busy = accountState.busyAction == AccountAction.CHANGE_USERNAME,
            onDismiss = { activeDialog = null; fieldErrorRes = null },
            onConfirm = {
                if (newUsername.isBlank() || currentPassword.isBlank()) fieldErrorRes = R.string.common_error
                else {
                    viewModel.changeUsername(newUsername, currentPassword) {
                        creditsViewModel.refresh()
                    }
                }
            },
        )
        AccountDialog.DELETE -> DeleteAccountDialog(
            busy = accountState.busyAction == AccountAction.DELETE_ACCOUNT,
            onDismiss = { activeDialog = null; fieldErrorRes = null },
            onConfirm = {
                viewModel.deleteAccount {
                    activeDialog = null
                    fieldErrorRes = null
                }
            },
        )
        null -> {}
    }

    // ---- Veri dışa aktarımı dialog'u (C5) ----
    ExportDataDialog(
        exported = accountState.exported,
        errorRes = accountState.exportErrorRes,
        loading = accountState.busyAction == AccountAction.EXPORT,
        onDismiss = { viewModel.dismissExport() },
    )
}

private fun validatePasswordChange(current: String, new: String): Int? = when {
    current.isBlank() || new.isBlank() -> R.string.common_error
    new.length < 10 -> R.string.auth_password_too_short
    else -> null
}

@Composable
private fun AccountActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FlorencePalettes.Florence.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    currentPassword: String,
    newPassword: String,
    onCurrentChange: (String) -> Unit,
    onNewChange: (String) -> Unit,
    errorRes: Int?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.account_change_password)) },
        text = {
            Column {
                AccountPasswordField(
                    value = currentPassword,
                    onValueChange = onCurrentChange,
                    label = stringResource(R.string.account_current_password),
                )
                Spacer(Modifier.height(10.dp))
                AccountPasswordField(
                    value = newPassword,
                    onValueChange = onNewChange,
                    label = stringResource(R.string.account_new_password),
                )
                errorRes?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !busy) {
                if (busy) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.account_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.account_cancel))
            }
        },
    )
}

@Composable
private fun ChangeEmailDialog(
    newEmail: String,
    currentPassword: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    errorRes: Int?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.account_change_email)) },
        text = {
            Column {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.account_new_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                AccountPasswordField(
                    value = currentPassword,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.account_current_password),
                )
                errorRes?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !busy) {
                if (busy) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.account_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.account_cancel))
            }
        },
    )
}

@Composable
private fun ChangeUsernameDialog(
    newUsername: String,
    currentPassword: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    errorRes: Int?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.account_change_username)) },
        text = {
            Column {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = onUsernameChange,
                    label = { Text(stringResource(R.string.account_new_username)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                AccountPasswordField(
                    value = currentPassword,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.account_current_password),
                )
                errorRes?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !busy) {
                if (busy) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.account_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.account_cancel))
            }
        },
    )
}

@Composable
private fun AccountPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DeleteAccountDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.account_delete_title)) },
        text = { Text(stringResource(R.string.account_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !busy) {
                if (busy) {
                    CircularProgressIndicator(
                        color = DownColor,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.account_delete), color = DownColor)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.account_cancel))
            }
        },
    )
}

@Composable
private fun ExportDataDialog(
    exported: String?,
    errorRes: Int?,
    loading: Boolean,
    onDismiss: () -> Unit,
) {
    if (exported != null || errorRes != null || loading) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.account_export_title)) },
            text = {
                Column {
                    when {
                        loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = stringResource(R.string.common_loading),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        errorRes != null -> Text(
                            text = stringResource(errorRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        exported.isNullOrBlank() -> Text(stringResource(R.string.account_export_empty))
                        else -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                                .height(320.dp),
                        ) {
                            Text(
                                text = exported,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.account_close))
                }
            },
        )
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    email: String?,
    isAdmin: Boolean,
    greeting: String,
    clock: String,
    avatarId: String?,
    onAvatarClick: () -> Unit,
) {
    val primary = FlorencePalettes.Florence.primary
    val secondary = FlorencePalettes.Florence.secondary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(primary.copy(alpha = 0.30f), MaterialTheme.colorScheme.background),
                )
            )
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Büyük avatar (kullanıcı avatar_id'si) — tıklayınca seçim ekranına gider.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickableNoRipple(onAvatarClick),
            ) {
                AvatarArt(avatarId = avatarId, size = 64.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.avatar_change),
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = clock,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                    )
                    if (isAdmin) {
                        Spacer(Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(UpColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UpColor,
                            )
                        }
                    }
                }
            }
        }
        email?.let {
            Spacer(Modifier.size(10.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
