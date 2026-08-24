package com.florence.app.presentation.advisor

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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.FitResultItem
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.formatCompact
import java.util.Locale

/**
 * G2 — AI Danışman: risk iştahı (muhafazakar/dengeli/agresif), ufuk ve karlılık
 * tercihiyle backend'in risk bazlı öneri ucu (/stocks/fit) üzerinden hisse önerir,
 * seçilen hisse için hızlı rapor üretir (kredi maliyeti önceden gösterilir).
 */
@Composable
fun AdvisorScreen(viewModel: AdvisorViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var ticker by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = null,
                    tint = FlorencePalettes.Florence.primary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.size(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.nav_advisor),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.advisor_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            AdvisorProfileCard(
                uiState = uiState,
                onRisk = viewModel::setRiskAppetite,
                onHorizon = viewModel::setHorizon,
                onProfitability = viewModel::setProfitability,
                onFit = viewModel::runFit,
            )
        }

        if (uiState.recommendations.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.advisor_recommendations_title))
            }
            items(uiState.recommendations, key = { it.ticker ?: System.identityHashCode(it) }) { rec ->
                AdvisorRecommendationRow(rec = rec, onSelect = { viewModel.select(rec.ticker ?: "") })
            }
        }

        item {
            AdvisorReportCard(
                uiState = uiState,
                ticker = ticker,
                onTickerChange = {
                    ticker = it.uppercase()
                    viewModel.select(it.uppercase())
                },
                onGenerate = { viewModel.generate(if (uiState.selected.isNullOrBlank()) ticker else (uiState.selected ?: "")) },
            )
        }

        if (uiState.message != null) {
            item {
                val res = uiState.messageRes
                if (res != null) {
                    Text(
                        text = stringResource(res),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.messageIsError) DownColor else UpColor,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                } else {
                    uiState.message?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.messageIsError) DownColor else UpColor,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }

        if (uiState.recommendations.isEmpty() && !uiState.fitting) {
            item {
                Text(
                    text = stringResource(R.string.advisor_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun AdvisorProfileCard(
    uiState: com.florence.app.presentation.advisor.AdvisorViewModel.AdvisorUiState,
    onRisk: (String) -> Unit,
    onHorizon: (String) -> Unit,
    onProfitability: (String) -> Unit,
    onFit: () -> Unit,
) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.advisor_profile_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            ChipRow(
                label = stringResource(R.string.advisor_risk_label),
                options = listOf(
                    Pair(R.string.advisor_risk_conservative, AdvisorViewModel.RISK_CONSERVATIVE),
                    Pair(R.string.advisor_risk_balanced, AdvisorViewModel.RISK_BALANCED),
                    Pair(R.string.advisor_risk_aggressive, AdvisorViewModel.RISK_AGGRESSIVE),
                ),
                selected = uiState.riskAppetite,
                onSelect = onRisk,
            )

            ChipRow(
                label = stringResource(R.string.advisor_horizon_label),
                options = listOf(
                    Pair(R.string.advisor_horizon_short, "short"),
                    Pair(R.string.advisor_horizon_medium, "medium"),
                    Pair(R.string.advisor_horizon_long, "long"),
                ),
                selected = uiState.horizon,
                onSelect = onHorizon,
            )

            ChipRow(
                label = stringResource(R.string.advisor_profit_label),
                options = listOf(
                    Pair(R.string.advisor_profit_low, "low"),
                    Pair(R.string.advisor_profit_medium, "medium"),
                    Pair(R.string.advisor_profit_high, "high"),
                ),
                selected = uiState.profitability,
                onSelect = onProfitability,
            )

            Button(
                onClick = onFit,
                enabled = !uiState.fitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.fitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.advisor_fitting))
                } else {
                    Text(stringResource(R.string.advisor_get_recommendations))
                }
            }
        }
    }
}

@Composable
private fun ChipRow(
    label: String,
    options: List<Pair<Int, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (labelRes, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }
    }
}

@Composable
private fun AdvisorRecommendationRow(rec: FitResultItem, onSelect: () -> Unit) {
    FlorenceCard(modifier = Modifier.fillMaxWidth(), onClick = onSelect) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = FlorencePalettes.Florence.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = rec.ticker ?: "—",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.advisor_score, rec.score?.let { "%.2f".format(Locale.US, it) } ?: "—"),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun AdvisorReportCard(
    uiState: com.florence.app.presentation.advisor.AdvisorViewModel.AdvisorUiState,
    ticker: String,
    onTickerChange: (String) -> Unit,
    onGenerate: () -> Unit,
) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.advisor_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = if (uiState.selected.isNullOrBlank()) ticker else (uiState.selected ?: ""),
                onValueChange = onTickerChange,
                label = { Text(stringResource(R.string.advisor_ticker_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(
                    R.string.advisor_cost,
                    uiState.info?.quickReport?.creditCost?.let { formatCompact(it) } ?: "—",
                    uiState.info?.quickReport?.estimatedSeconds?.toString() ?: "…",
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onGenerate,
                enabled = !uiState.generating && (uiState.selected?.isNotBlank() == true || ticker.isNotBlank()),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    if (uiState.generating) stringResource(R.string.advisor_analyzing)
                    else stringResource(R.string.advisor_analyze),
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
                    text = stringResource(R.string.advisor_credit_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}