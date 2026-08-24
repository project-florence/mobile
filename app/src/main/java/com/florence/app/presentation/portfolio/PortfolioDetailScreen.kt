package com.florence.app.presentation.portfolio

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.PortfolioAsset
import com.florence.app.data.model.PortfolioTransaction
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.TickerAvatar
import com.florence.app.presentation.components.formatCompact
import com.florence.app.presentation.components.formatPrice

@Composable
fun PortfolioDetailScreen(
    onBack: () -> Unit,
    onOpenAnalytics: () -> Unit,
    viewModel: PortfolioDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    val valuation = uiState.snapshot?.valuation

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Başlık
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.snapshot?.portfolio?.name ?: "Portföy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Sanal portföy · ${formatPrice(valuation?.totalValue ?: 0.0)} ₺",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                    }
                }
            }

            // Analitikler girişi → portfolio/{id}/analytics ekranı
            item {
                Button(
                    onClick = onOpenAnalytics,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.portfolio_open_analytics))
                }
            }

            if (uiState.loading && valuation == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // Özet kartlar
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatBox(
                            label = "Toplam Değer",
                            value = formatPrice(valuation?.totalValue ?: 0.0) + " ₺",
                            modifier = Modifier.weight(1f),
                        )
                        StatBox(
                            label = "Nakit",
                            value = formatPrice(valuation?.cashBalance ?: 0.0) + " ₺",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatBox(
                            label = "Pozisyonlar",
                            value = formatPrice(valuation?.holdingsValue ?: 0.0) + " ₺",
                            modifier = Modifier.weight(1f),
                        )
                        val pnl = valuation?.totalPnl ?: 0.0
                        StatBox(
                            label = "Kâr/Zarar",
                            value = (if (pnl >= 0) "+" else "") + "%.2f ₺".format(pnl),
                            valueColor = if (pnl >= 0) UpColor else DownColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Pozisyonlar
                item {
                    Text(
                        text = "Pozisyonlar (${uiState.snapshot?.valuation?.assets?.size ?: 0})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (uiState.snapshot?.valuation?.assets.isNullOrEmpty()) {
                    item {
                        EmptyState(
                            title = "Pozisyon yok",
                            subtitle = "İşlem ekleyerek sanal alım-satım yapmaya başla.",
                        )
                    }
                } else {
                    items(uiState.snapshot?.valuation?.assets ?: emptyList(), key = { it.ticker ?: it.hashCode() }) { asset ->
                        AssetRow(asset)
                    }
                }

                // İşlem geçmişi
                item {
                    Text(
                        text = "İşlem Geçmişi (${uiState.transactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (uiState.transactions.isEmpty()) {
                    item {
                        EmptyState(
                            title = "Henüz işlem yok",
                            subtitle = "Aşağıdaki butondan ilk işlemini ekle.",
                        )
                    }
                } else {
                    items(uiState.transactions, key = { it.id ?: it.hashCode() }) { tx ->
                        TransactionRow(tx)
                    }
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

        // İşlem ekleme FAB
        if (!uiState.loading) {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAdd = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "İşlem ekle")
            }
        }
    }

    if (showAdd) {
        AddTransactionDialog(
            busy = uiState.adding,
            onDismiss = {
                showAdd = false
                viewModel.clearMessage()
            },
            onAdd = { ticker, type, qty ->
                viewModel.addTransaction(ticker, type, qty)
                showAdd = false
            },
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    FlorenceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun AssetRow(asset: PortfolioAsset) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TickerAvatar(ticker = asset.ticker ?: "?", size = 40.dp)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.ticker ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${asset.amount?.let { "%.2f".format(it) } ?: "0"} adet · maliyet ${formatPrice(asset.weightedAvgCost ?: 0.0)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatPrice(asset.totalValue ?: 0.0) + " ₺",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val pnl = asset.unrealizedPnl ?: 0.0
                Text(
                    text = (if (pnl >= 0) "+" else "") + "%.2f ₺".format(pnl),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (pnl >= 0) UpColor else DownColor,
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: PortfolioTransaction) {
    val isBuy = tx.type == "BUY"
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isBuy) UpColor.copy(alpha = 0.18f) else DownColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isBuy) "A" else "S",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isBuy) UpColor else DownColor,
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tx.ticker ?: "—"} · ${if (isBuy) "ALIŞ" else "SATIŞ"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${tx.quantity?.let { "%.2f".format(it) } ?: "0"} adet × ${formatPrice(tx.price ?: 0.0)} · ${tx.date?.take(10) ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatPrice(tx.total ?: 0.0) + " ₺",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AddTransactionDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double) -> Unit,
) {
    var ticker by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BUY") }
    var quantity by remember { mutableStateOf("") }

    val qty = quantity.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("İşlem Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "BUY",
                        onClick = { type = "BUY" },
                        label = { Text("ALIŞ") },
                    )
                    FilterChip(
                        selected = type == "SELL",
                        onClick = { type = "SELL" },
                        label = { Text("SATIŞ") },
                    )
                }
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Hisse kodu (örn. THYAO)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Miktar (adet)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "İşlem fiyatı güncel piyasa değerinden otomatik alınır.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = ticker.isNotBlank() && qty != null && qty > 0 && !busy,
                onClick = { qty?.let { q -> onAdd(ticker, type, q) } },
            ) {
                Text(if (busy) "Ekleniyor…" else "Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Vazgeç") }
        },
    )
}
