package com.florence.app.presentation.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.SimulationDetailResponse
import com.florence.app.data.model.SimulationResponse
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.formatPrice
import java.util.Locale

/**
 * G1 — Monte Carlo simülasyonu.
 * Yeni simülasyon çalıştırır (kredi harcar), maliyet önizlemesi yapar ve geçmişi listeler.
 */
@Composable
fun SimulationScreen(viewModel: SimulationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var ticker by rememberSaveable { mutableStateOf("") }
    var daysText by rememberSaveable { mutableStateOf("90") }
    var boundsText by rememberSaveable { mutableStateOf("0.05") }
    var targetText by rememberSaveable { mutableStateOf("") }

    val days = daysText.toIntOrNull()
    val target = targetText.takeIf { it.isNotBlank() }

    // Geçerli giriş olduğunda maliyet önizlemesini tazele.
    LaunchedEffect(ticker, days) {
        viewModel.estimate(ticker, days ?: 0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = FlorencePalettes.Florence.primary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.size(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.simulation_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.simulation_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text(stringResource(R.string.simulation_tab_run)) },
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text(stringResource(R.string.simulation_tab_history)) },
                )
            }
        }

        if (tab == 0) {
            item {
                SimulationRunCard(
                    uiState = uiState,
                    ticker = ticker,
                    onTickerChange = { ticker = it.uppercase() },
                    daysText = daysText,
                    onDaysChange = { daysText = it.filter(Char::isDigit).take(3) },
                    boundsText = boundsText,
                    onBoundsChange = { boundsText = it },
                    targetText = targetText,
                    onTargetChange = { targetText = it },
                    days = days,
                    onRun = {
                        viewModel.runSimulation(ticker, days ?: 90, boundsText, target)
                    },
                    onClearMessage = viewModel::clearMessage,
                )
            }
            uiState.result?.let { result ->
                item { SimulationResultCard(result = result) }
            }
        } else {
            item {
                SimulationHistoryList(
                    uiState = uiState,
                    onSelect = viewModel::loadDetail,
                )
            }
            uiState.detail?.let { detail ->
                item { SimulationDetailCard(detail = detail) }
            }
        }

        uiState.message?.let { raw ->
            item {
                val res = uiState.messageRes
                val text = if (res != null) stringResource(res) else raw
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.messageIsError) DownColor else UpColor,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SimulationRunCard(
    uiState: com.florence.app.presentation.simulation.SimulationViewModel.SimulationUiState,
    ticker: String,
    onTickerChange: (String) -> Unit,
    daysText: String,
    onDaysChange: (String) -> Unit,
    boundsText: String,
    onBoundsChange: (String) -> Unit,
    targetText: String,
    onTargetChange: (String) -> Unit,
    days: Int?,
    onRun: () -> Unit,
    onClearMessage: () -> Unit,
) {
    val runEnabled = ticker.isNotBlank() && days != null && days in 1..370 && !uiState.simulating
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.simulation_run_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(
                value = ticker,
                onValueChange = onTickerChange,
                label = { Text(stringResource(R.string.simulation_ticker_label)) },
                placeholder = { Text(stringResource(R.string.simulation_ticker_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = daysText,
                onValueChange = onDaysChange,
                label = { Text(stringResource(R.string.simulation_days_label)) },
                supportingText = { Text(stringResource(R.string.simulation_days_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = days != null && (days < 1 || days > 370),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = boundsText,
                onValueChange = onBoundsChange,
                label = { Text(stringResource(R.string.simulation_bounds_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = targetText,
                onValueChange = onTargetChange,
                label = { Text(stringResource(R.string.simulation_target_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.simulation_estimate, uiState.estimateCost?.let {
                    "%.3f".format(Locale.US, it)
                } ?: "—"),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.simulation_credit_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onRun,
                enabled = runEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.simulating) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.simulation_running))
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.simulation_run))
                }
            }
        }
    }
}

@Composable
private fun SimulationResultCard(result: SimulationResponse) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.simulation_result_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            result.simulationId?.let {
                Text(
                    text = "#$it · ${result.ticker ?: ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            val probAbove = result.probAbove?.times(100)
            val probBelow = result.probBelow?.times(100)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultStat(
                    label = stringResource(R.string.simulation_prob_above),
                    value = probAbove?.let { "%.1f%%".format(Locale.US, it) } ?: "—",
                    color = if (probAbove != null && probAbove >= 50) UpColor else DownColor,
                    modifier = Modifier.weight(1f),
                )
                ResultStat(
                    label = stringResource(R.string.simulation_prob_below),
                    value = probBelow?.let { "%.1f%%".format(Locale.US, it) } ?: "—",
                    color = if (probBelow != null && probBelow >= 50) UpColor else DownColor,
                    modifier = Modifier.weight(1f),
                )
            }

            result.confidence?.let { conf ->
                val lvl = conf.percent?.times(100)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultStat(
                        label = stringResource(R.string.simulation_conf_range, lvl?.let { "%.0f".format(Locale.US, it) } ?: "—"),
                        value = "${formatPrice(conf.min ?: 0.0)} – ${formatPrice(conf.max ?: 0.0)}",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            result.direction?.let {
                Text(
                    text = stringResource(R.string.simulation_direction, it),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            result.target?.let {
                Text(
                    text = stringResource(R.string.simulation_target_value, it),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            result.creditsSpend?.let {
                Text(
                    text = stringResource(R.string.simulation_cost_spent, "%.3f".format(Locale.US, it)),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            result.remainingCredits?.let {
                Text(
                    text = stringResource(R.string.simulation_credits_left, "%.1f".format(Locale.US, it)),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = UpColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.simulation_result_saved),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ResultStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    FlorenceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun SimulationHistoryList(
    uiState: com.florence.app.presentation.simulation.SimulationViewModel.SimulationUiState,
    onSelect: (Int) -> Unit,
) {
    if (uiState.historyLoading && uiState.history.isEmpty()) {
        EmptyState(text = stringResource(R.string.common_loading))
        return
    }
    if (uiState.history.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.simulation_empty_history),
            subtitle = stringResource(R.string.simulation_empty_history_hint),
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        uiState.history.forEach { item ->
            FlorenceCard(modifier = Modifier.fillMaxWidth(), onClick = { item.id?.let(onSelect) }) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.simulation_history_line, item.ticker ?: "—", item.days ?: 0),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.simulation_history_meta, item.createdAt ?: "", item.target ?: "auto"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = item.cost?.let { "%.3f".format(Locale.US, it) } ?: "—",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulationDetailCard(detail: SimulationDetailResponse) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.simulation_detail_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.simulation_detail_meta, detail.ticker ?: "—", detail.days ?: 0, detail.target ?: "auto"),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            detail.result?.let { result ->
                val above = result.probAbove?.times(100)
                Text(
                    text = stringResource(
                        R.string.simulation_detail_probs,
                        above?.let { "%.1f".format(Locale.US, it) } ?: "—",
                        result.probBelow?.times(100)?.let { "%.1f".format(Locale.US, it) } ?: "—",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                result.confidence?.let { conf ->
                    Text(
                        text = stringResource(
                            R.string.simulation_detail_confidence,
                            formatPrice(conf.min ?: 0.0),
                            formatPrice(conf.max ?: 0.0),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                result.creditsSpend?.let {
                    Text(
                        text = stringResource(R.string.simulation_cost_spent, "%.3f".format(Locale.US, it)),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}