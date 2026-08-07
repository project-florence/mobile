package com.florence.app.presentation.company

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.MarketData
import com.florence.app.data.model.NewsItem
import com.florence.app.presentation.components.CandleChart
import com.florence.app.presentation.components.ChangePill
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.clickableNoRipple
import com.florence.app.presentation.components.formatCompact
import com.florence.app.presentation.components.formatPrice
import com.florence.app.presentation.home.computeChangePct
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    onBack: () -> Unit,
    viewModel: CompanyDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = viewModel.ticker,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    uiState.info?.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            },
            actions = {
                IconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (uiState.isFavorite) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            TextSecondary.copy(alpha = 0.35f)
                        },
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        when {
            uiState.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            uiState.error && uiState.info == null -> EmptyState(
                stringResource(R.string.dashboard_error),
                modifier = Modifier.fillMaxSize(),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { PriceHeader(uiState.info?.market) }

                item {
                    FlorenceCard {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.detail_price_history),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = stringResource(uiState.range.labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            if (uiState.candles.isEmpty()) {
                                EmptyState(stringResource(R.string.detail_no_history))
                            } else {
                                CandleChart(candles = uiState.candles)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CHART_RANGES) { range ->
                            FilterChip(
                                selected = uiState.range == range,
                                onClick = { viewModel.selectRange(range) },
                                label = { Text(stringResource(range.labelRes)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                }

                item {
                    FlorenceCard {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            uiState.info?.market?.let { market ->
                                StatRow(stringResource(R.string.detail_market_cap), "${formatCompact(market.marketCap)} ₺")
                                StatRow(stringResource(R.string.detail_day_range), dayRange(market))
                                StatRow(stringResource(R.string.detail_52w), weekRange(market))
                                StatRow(stringResource(R.string.detail_volume), formatCompact(market.volume))
                            }
                            uiState.info?.sector?.let {
                                StatRow(stringResource(R.string.detail_sector), it)
                            }
                            uiState.info?.industry?.let {
                                StatRow(stringResource(R.string.detail_industry), it)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.detail_news),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (uiState.news.isEmpty()) {
                    item {
                        if (uiState.newsError) {
                            EmptyState(stringResource(R.string.detail_news_unavailable))
                        } else {
                            EmptyState(stringResource(R.string.detail_no_news))
                        }
                    }
                } else {
                    items(uiState.news, key = { it.url ?: it.hashCode() }) { news ->
                        NewsRow(news) { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceHeader(market: MarketData?) {
    val price = market?.currentPrice
    val changePct = computeChangePct(market)
    val color = when {
        changePct == null -> MaterialTheme.colorScheme.onSurface
        changePct >= 0 -> UpColor
        else -> DownColor
    }
    Column {
        Text(
            text = if (price != null) formatPrice(price) else "—",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChangePill(changePct)
            market?.previousClose?.let {
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.detail_prev_close, formatPrice(it)),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NewsRow(news: NewsItem, onOpen: (String) -> Unit) {
    val url = news.url ?: return
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .clickableNoRipple { onOpen(url) }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = news.title ?: "",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            news.summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                news.source?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (news.source != null && news.date != null) {
                    Text(text = " • ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                news.date?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.common_open),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun dayRange(market: MarketData): String {
    val low = market.dayLow ?: return "—"
    val high = market.dayHigh ?: return "—"
    return String.format(Locale.US, "%.2f – %.2f", low, high)
}

private fun weekRange(market: MarketData): String {
    val low = market.fiftyTwoWeekLow ?: return "—"
    val high = market.fiftyTwoWeekHigh ?: return "—"
    return String.format(Locale.US, "%.2f – %.2f", low, high)
}
