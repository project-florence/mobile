package com.florence.app.presentation.bots

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.data.model.BotItem
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import android.widget.Toast

@Composable
fun BotsScreen(
    viewModel: BotsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<BotItem?>(null) }
    var copied by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ---- Başlık + limit ---- 
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.bots_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(
                        R.string.bots_limit,
                        uiState.bots.size,
                        BotsViewModel.MAX_BOTS,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }

        // ---- Oluşturma formu ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; usernameError = false },
                        label = { Text(stringResource(R.string.bots_username)) },
                        placeholder = { Text(stringResource(R.string.bots_username_hint)) },
                        singleLine = true,
                        isError = usernameError,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (usernameError) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.bots_username_required),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.bots_password)) },
                        placeholder = { Text(stringResource(R.string.bots_password_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (username.isBlank()) {
                                usernameError = true
                            } else {
                                viewModel.create(username, password)
                                username = ""
                                password = ""
                            }
                        },
                        enabled = !uiState.creating,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.creating) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                        Text(if (uiState.creating) stringResource(R.string.bots_creating) else stringResource(R.string.bots_create))
                    }
                }
            }
        }

        // ---- Hata ----
        uiState.errorRes?.let { err ->
            item {
                Text(
                    text = stringResource(err),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        // ---- Liste ----
        when {
            uiState.loading -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.bots.isEmpty() -> item {
                EmptyState(
                    title = stringResource(R.string.bots_empty),
                    subtitle = stringResource(R.string.bots_empty_hint),
                )
            }

            else -> items(uiState.bots, key = { it.id ?: it.hashCode() }) { bot ->
                BotRow(
                    bot = bot,
                    deleting = uiState.deletingId == bot.id,
                    onDelete = { deleteTarget = bot },
                )
            }
        }
    }

    // ---- Silme onayı ----
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.bots_delete_title)) },
            text = { Text(stringResource(R.string.bots_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.bots_delete), color = DownColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.bots_cancel))
                }
            },
        )
    }

    // ---- Tek seferlik şifre ----
    val generatedPassword = uiState.createdPassword
    if (generatedPassword != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissCreatedPassword()
            },
            title = { Text(stringResource(R.string.bots_password_title)) },
            text = {
                val copiedToast = stringResource(R.string.bots_copied)
                Column {
                    Text(
                        text = stringResource(R.string.bots_password_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                clipboard.setText(AnnotatedString(generatedPassword))
                                copied = true
                                Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = generatedPassword,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text(
                        text = if (copied) stringResource(R.string.bots_copied) else stringResource(R.string.bots_copy),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCreatedPassword() }) {
                    Text(stringResource(R.string.bots_created))
                }
            },
        )
    }
}

@Composable
private fun BotRow(
    bot: BotItem,
    deleting: Boolean,
    onDelete: () -> Unit,
) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = null,
                    tint = FlorencePalettes.Florence.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bot.username ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                bot.createdAt?.let {
                    Text(
                        text = stringResource(R.string.bots_created_at, it.take(10)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
            IconButton(onClick = onDelete, enabled = !deleting) {
                if (deleting) {
                    CircularProgressIndicator(
                        color = DownColor,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.bots_delete),
                        tint = DownColor,
                    )
                }
            }
        }
    }
}
