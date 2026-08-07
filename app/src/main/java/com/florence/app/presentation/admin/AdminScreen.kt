package com.florence.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.AdminUser
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.TickerAvatar

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var target by remember { mutableStateOf<AdminUser?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Admin Paneli",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Hesaplara coin (kredi) aktarımı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
            }
        }

        when {
            uiState.loading && uiState.users.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.error != null && uiState.users.isEmpty() -> {
                EmptyState(
                    title = "Erişim reddedildi",
                    subtitle = "Bu panel yalnızca admin hesaplarına açıktır.",
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.users, key = { it.id ?: it.hashCode() }) { user ->
                        AdminUserRow(
                            user = user,
                            onTransfer = { target = user },
                        )
                    }
                    uiState.message?.let { msg ->
                        item {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.messageIsError) DownColor else UpColor,
                            )
                        }
                    }
                }
            }
        }
    }

    target?.let { user ->
        TransferDialog(
            user = user,
            busy = uiState.transferring,
            onDismiss = {
                target = null
                viewModel.clearMessage()
            },
            onTransfer = { amount ->
                viewModel.transfer(user.id ?: 0L, amount)
                target = null
            },
        )
    }
}

@Composable
private fun AdminUserRow(user: AdminUser, onTransfer: () -> Unit) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TickerAvatar(ticker = user.username ?: "?", size = 40.dp)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = user.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = user.credits?.let { "%.1f".format(it) } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (user.userType == "admin") "admin" else "kullanıcı",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.userType == "admin") UpColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(8.dp))
            IconButton(onClick = onTransfer) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Coin gönder",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TransferDialog(
    user: AdminUser,
    busy: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (Double) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    val amountNum = amount.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Coin Gönder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Hesap: ${user.username} (mevcut: ${user.credits?.let { "%.1f".format(it) } ?: "—"} coin)",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Miktar (negatif = düş)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = amountNum != null && amountNum != 0.0 && !busy,
                onClick = { amountNum?.let(onTransfer) },
            ) {
                Text(if (busy) "Gönderiliyor…" else "Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Vazgeç") }
        },
    )
}
