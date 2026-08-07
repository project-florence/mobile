package com.florence.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.model.Ticker
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            uiState.version?.let {
                Text(
                    text = stringResource(R.string.dashboard_version, it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_retry))
            }
        }

        when {
            uiState.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.dashboard_loading),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            uiState.error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.dashboard_error))
                    Spacer(Modifier.size(8.dp))
                    TextButton(onClick = viewModel::refresh) {
                        Text(stringResource(R.string.dashboard_retry))
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (uiState.disabledFeatures.isNotEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(
                                        R.string.dashboard_maintenance,
                                        uiState.disabledFeatures.joinToString(", "),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.dashboard_currencies),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (uiState.currencies.isEmpty()) {
                        item { Text(stringResource(R.string.dashboard_error), style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(uiState.currencies.take(12)) { (code, quote) ->
                            CurrencyRow(code = code, quote = quote)
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.dashboard_stocks),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                    if (uiState.tickers.isEmpty()) {
                        item { Text(stringResource(R.string.dashboard_error), style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(uiState.tickers.take(25)) { ticker ->
                            TickerRow(ticker)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(code: String, quote: CurrencyQuote) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = code, style = MaterialTheme.typography.titleSmall)
                quote.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val rate = quote.selling ?: quote.buying ?: quote.rate
            if (rate != null) {
                Text(
                    text = String.format(Locale.US, "%.4f", rate),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            quote.changePct?.let { pct ->
                val positive = pct >= 0
                Text(
                    text = String.format(Locale.US, "%+.2f%%", pct),
                    color = if (positive) UpColor else DownColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TickerRow(ticker: Ticker) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = ticker.ticker ?: "-",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            val name = ticker.companyName ?: ticker.name
            if (!name.isNullOrBlank()) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
