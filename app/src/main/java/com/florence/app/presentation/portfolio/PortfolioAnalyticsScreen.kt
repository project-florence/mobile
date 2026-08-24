package com.florence.app.presentation.portfolio

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.BenchmarkComparison
import com.florence.app.data.model.PortfolioDiversification
import com.florence.app.data.model.PortfolioPerformance
import com.florence.app.data.model.PortfolioPerformer
import com.florence.app.data.model.PortfolioReturns
import com.florence.app.data.model.PortfolioRisk
import com.florence.app.data.model.PortfolioValuation
import com.florence.app.data.model.TransactionStats
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.formatCompact
import com.florence.app.presentation.components.formatPrice
import com.florence.app.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioAnalyticsScreen(
    onBack: () -> Unit,
    viewModel: PortfolioAnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (state.loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.error != null && state.valuation == null) {
                item { EmptyState(state.error ?: "") }
                return@LazyColumn
            }

            // ---- Özet (valuation) ----
            val valuation = state.valuation
            if (valuation != null) {
                item { SectionHeader(stringResource(R.string.analytics_overview)) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatBox(
                            label = stringResource(R.string.analytics_total_value),
                            value = formatPrice(valuation.totalValue ?: 0.0) + " ₺",
                            modifier = Modifier.weight(1f),
                        )
                        StatBox(
                            label = stringResource(R.string.analytics_total_pnl),
                            value = formatSignedPrice(valuation.totalPnl),
                            valueColor = pnlColor(valuation.totalPnl),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatBox(
                            label = stringResource(R.string.analytics_cash),
                            value = formatPrice(valuation.cashBalance ?: 0.0) + " ₺",
                            modifier = Modifier.weight(1f),
                        )
                        StatBox(
                            label = stringResource(R.string.analytics_pnl_pct),
                            value = valuation.pnlPercentage?.let { formatCompact(it) + "%" } ?: "—",
                            valueColor = pnlColor(valuation.totalPnl),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ---- Dağılım (diversification) ----
            val div = state.diversification
            if (div != null && div.assets.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.analytics_diversification)) }
                item {
                    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row {
                                StatMini(stringResource(R.string.analytics_cash), div.cashAllocationPct?.let { "%.0f%%".format(it) } ?: "—", Modifier.weight(1f))
                                StatMini(stringResource(R.string.analytics_by_type), div.allocationByType.map { (k, v) -> "${k.uppercase()}: %.0f%%".format(v) }.joinToString(" · "), Modifier.weight(1f))
                            }
                            div.assets.take(5).forEach { asset ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(asset.ticker ?: "—", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text(
                                        text = asset.allocationPct?.let { "%.1f%%".format(it) } ?: "—",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---- En iyi / en kötü (performers) ----
            val performers = state.performers
            if (performers != null && (performers.best.isNotEmpty() || performers.worst.isNotEmpty())) {
                item { SectionHeader(stringResource(R.string.analytics_performers)) }
                item {
                    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (performers.best.isNotEmpty()) {
                                TopLine(stringResource(R.string.analytics_best))
                                performers.best.take(3).forEach { PerformerRow(it, best = true) }
                            }
                            if (performers.worst.isNotEmpty()) {
                                Spacer(Modifier.size(6.dp))
                                TopLine(stringResource(R.string.analytics_worst))
                                performers.worst.take(3).forEach { PerformerRow(it, best = false) }
                            }
                        }
                    }
                }
            }

            // ---- Getiri (returns) ----
            state.returns?.let { ret ->
                item { SectionHeader(stringResource(R.string.analytics_returns)) }
                item { ReturnsCard(ret) }
            }

            // ---- Risk (risk) ----
            state.risk?.let { risk ->
                item { SectionHeader(stringResource(R.string.analytics_risk)) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatBox(
                            label = stringResource(R.string.analytics_volatility),
                            value = risk.volatility?.let { "%.2f%%".format(it) } ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                        StatBox(
                            label = stringResource(R.string.analytics_max_drawdown),
                            value = risk.maxDrawdown?.let { "-%.2f%%".format(it) } ?: "—",
                            valueColor = DownColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    StatBox(
                        label = stringResource(R.string.analytics_sharpe),
                        value = risk.sharpeRatio?.let { "%.2f".format(it) } ?: "—",
                    )
                }
            }

            // ---- Benchmark ----
            state.benchmark?.let { bench ->
                item { SectionHeader(stringResource(R.string.analytics_benchmark)) }
                item { BenchmarkCard(bench) }
            }

            // ---- Performance ----
            state.performance?.let { perf ->
                item { SectionHeader(stringResource(R.string.analytics_performance)) }
                item { PerformanceCard(perf) }
            }

            // ---- Stats ----
            state.stats?.let { stats ->
                item { SectionHeader(stringResource(R.string.analytics_stats)) }
                item { StatsCard(stats) }
            }

            // ---- Geçmiş (history) ----
            if (state.history.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.analytics_history)) }
                item {
                    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.history.takeLast(8).forEach { pt ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pt.ts?.take(10) ?: "—",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(formatPrice(pt.totalValue ?: 0.0) + " ₺", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun TopLine(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun PerformerRow(p: PortfolioPerformer, best: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(p.ticker ?: "—", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        val pnl = p.pnl ?: 0.0
        Text(
            text = (if (pnl >= 0) "+" else "") + "%.2f ₺".format(pnl),
            style = MaterialTheme.typography.labelLarge,
            color = if (best) UpColor else DownColor,
        )
    }
}

@Composable
private fun ReturnsCard(ret: PortfolioReturns) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val color = pnlColor(ret.absoluteReturn)
            Text(
                text = stringResource(R.string.analytics_total_return) + " · " +
                    (if ((ret.absoluteReturn ?: 0.0) >= 0) "+" else "") +
                    "%.2f ₺".format(ret.absoluteReturn ?: 0.0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = stringResource(R.string.analytics_return_pct) + ": " +
                    (ret.totalReturnPercentage?.let { "%.2f%%".format(it) } ?: "—"),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "CAGR: " + (ret.cagrPercentage?.let { "%.2f%%".format(it) } ?: "—"),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.analytics_period) + ": " + (ret.period?.uppercase() ?: "—"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BenchmarkCard(bench: BenchmarkComparison) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                StatMini(
                    stringResource(R.string.analytics_portfolio_return),
                    bench.portfolioReturnPct?.let { "%.2f%%".format(it) } ?: "—",
                    Modifier.weight(1f),
                )
                StatMini(
                    (bench.benchmarkTicker ?: "XU100"),
                    bench.benchmarkReturnPct?.let { "%.2f%%".format(it) } ?: "—",
                    Modifier.weight(1f),
                )
            }
            val diff = bench.differencePct
            Text(
                text = stringResource(R.string.analytics_outperformed) + ": " +
                    (if (bench.outperformed == true) stringResource(R.string.analytics_yes) else stringResource(R.string.analytics_no)),
                style = MaterialTheme.typography.bodyMedium,
                color = if (diff != null && diff >= 0) UpColor else DownColor,
            )
        }
    }
}

@Composable
private fun PerformanceCard(perf: PortfolioPerformance) {
    val overall = perf.overall
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (overall != null) {
                Text(
                    text = stringResource(R.string.analytics_efficiency) + ": " +
                        (overall.efficiencyScore?.let { "%.0f%%".format(it * 100) } ?: "—"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row {
                    StatMini(stringResource(R.string.analytics_actual_pnl), formatSignedPrice(overall.actualPnl), Modifier.weight(1f))
                    StatMini(stringResource(R.string.analytics_optimal_pnl), formatSignedPrice(overall.optimalPnl), Modifier.weight(1f))
                }
            } else {
                Text(stringResource(R.string.analytics_no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (perf.assets.isNotEmpty()) {
                Spacer(Modifier.size(4.dp))
                perf.assets.take(3).forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(a.ticker ?: "—", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.analytics_efficiency) + ": " +
                                (a.efficiencyScore?.let { "%.0f%%".format(it * 100) } ?: "—"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(stats: TransactionStats) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                StatMini(stringResource(R.string.analytics_tx_total), stats.totalTransactions?.toString() ?: "—", Modifier.weight(1f))
                StatMini(stringResource(R.string.analytics_tx_buys), stats.totalBuys?.toString() ?: "—", Modifier.weight(1f))
                StatMini(stringResource(R.string.analytics_tx_sells), stats.totalSells?.toString() ?: "—", Modifier.weight(1f))
            }
            Row {
                StatMini(stringResource(R.string.analytics_tx_unique), stats.uniqueTickers?.toString() ?: "—", Modifier.weight(1f))
                StatMini(
                    stringResource(R.string.analytics_tx_avg),
                    formatPrice(stats.avgTransactionSize ?: 0.0),
                    Modifier.weight(1f),
                )
            }
        }
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
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun StatMini(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatSignedPrice(v: Double?): String {
    val value = v ?: 0.0
    return (if (value >= 0) "+" else "") + "%.2f ₺".format(value)
}

private fun pnlColor(v: Double?): androidx.compose.ui.graphics.Color =
    if ((v ?: 0.0) >= 0) UpColor else DownColor