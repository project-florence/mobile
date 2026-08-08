package com.florence.app.presentation.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.florence.app.data.model.CompanyInfo
import com.florence.app.data.model.MarketData
import com.florence.app.data.model.Ticker
import com.florence.app.presentation.components.ChangePill
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.LogoMark
import com.florence.app.presentation.components.SectionHeader
import com.florence.app.presentation.components.SkeletonBox
import com.florence.app.presentation.components.StatCard
import com.florence.app.presentation.components.Sparkline
import com.florence.app.presentation.components.TickerAvatar
import com.florence.app.presentation.components.clickableNoRipple
import com.florence.app.presentation.components.formatCompact
import com.florence.app.presentation.components.formatPrice
import com.florence.app.presentation.components.sparklineColor
import com.florence.app.presentation.profile.greetingResFor
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenCompany: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    creditsViewModel: com.florence.app.presentation.settings.CreditsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val creditsState by creditsViewModel.uiState.collectAsStateWithLifecycle()

    // Zaman ayarlı selamlama + canlı saat (30 sn'de bir güncellenir).
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(30_000)
        }
    }
    val username = creditsState.profile?.username ?: "Yatırımcı"
    val greeting = stringResource(greetingResFor(now.hour), username)
    val clock = now.format(DateTimeFormatter.ofPattern("HH:mm"))

    Column(modifier = Modifier.fillMaxSize()) {
        DashboardHeader(
            greeting = greeting,
            clock = clock,
            onRefresh = viewModel::refresh,
        )

        PullToRefreshBox(
            isRefreshing = uiState.loading && uiState.companies.isNotEmpty(),
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                uiState.loading && uiState.companies.isEmpty() -> DashboardSkeleton()

                uiState.error -> EmptyState(stringResource(R.string.dashboard_error))

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (uiState.disabledFeatures.isNotEmpty()) {
                        item {
                            FlorenceCard {
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

                    // Piyasa durumu kartı (açık/kapalı + öne çıkanlardan yükselen/düşen)
                    item {
                        MarketStatusCard(heroes = uiState.heroes)
                    }

                    item {
                        SectionHeader(title = stringResource(R.string.dashboard_highlights))
                        Spacer(Modifier.height(10.dp))
                        if (uiState.heroes.isEmpty()) {
                            EmptyState(stringResource(R.string.dashboard_no_data))
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(uiState.heroes, key = { it.symbol ?: it.hashCode() }) { info ->
                                    HeroCard(
                                        info = info,
                                        sparkline = uiState.sparklines[info.symbol],
                                        onClick = { ticker -> onOpenCompany(ticker) },
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(6.dp))
                        SectionHeader(title = stringResource(R.string.dashboard_all_stocks))
                        Spacer(Modifier.height(10.dp))
                    }
                    if (uiState.companies.isEmpty()) {
                        item { EmptyState(stringResource(R.string.dashboard_no_data)) }
                    } else {
                        items(uiState.companies, key = { it.ticker ?: it.hashCode() }) { company ->
                            CompanyRow(
                                company = company,
                                isFavorite = company.ticker?.let { it in uiState.favorites } == true,
                                onToggleFavorite = { ticker -> viewModel.toggleFavorite(ticker) },
                                onClick = { ticker -> onOpenCompany(ticker) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    greeting: String?,
    clock: String?,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Selamlama kutucuğu — ana sayfanın tek kimliği.
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            greeting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())) +
                    (clock?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onRefresh) {
            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_retry))
        }
    }
}

/** Öne çıkan hisse kartı: avatar + fiyat + değişim + mini grafik. */
@Composable
private fun HeroCard(
    info: CompanyInfo,
    sparkline: List<Float>?,
    onClick: (String) -> Unit,
) {
    val market = info.market
    val ticker = (info.symbol ?: "").removeSuffix(".IS")
    val price = market?.currentPrice
    val changePct = computeChangePct(market)
    val changeColor = when {
        changePct == null -> TextSecondary
        changePct >= 0 -> UpColor
        else -> DownColor
    }
    FlorenceCard(
        onClick = { onClick(ticker) },
        modifier = Modifier.width(172.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TickerAvatar(ticker = ticker, size = 34.dp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticker,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = changePct?.let { String.format(Locale.US, "%+.2f%%", it) } ?: "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (price != null) formatPrice(price) else "—",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            sparkline?.takeIf { it.size >= 2 }?.let { series ->
                Spacer(Modifier.height(8.dp))
                Sparkline(
                    values = series,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    lineColor = sparklineColor(series, UpColor, DownColor),
                )
            }
        }
    }
}

/** Piyasa durumu: açık/kapalı + öne çıkan hisselerin yükselen/düşen sayısı. */
@Composable
private fun MarketStatusCard(heroes: List<CompanyInfo>) {
    val now = LocalTime.now()
    val day = LocalDate.now().dayOfWeek
    val isWeekday = day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY
    val isOpen = isWeekday && now >= LocalTime.of(9, 30) && now <= LocalTime.of(18, 0)
    val ups = heroes.count { (computeChangePct(it.market) ?: 0.0) >= 0 }
    val downs = heroes.size - ups

    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOpen) UpColor.copy(alpha = 0.15f) else DownColor.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isOpen) "▲" else "▼",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isOpen) UpColor else DownColor,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOpen) "Piyasa Açık" else "Piyasa Kapalı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOpen) UpColor else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Hafta içi 09:30–18:00 · ${now.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$ups ▲",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UpColor,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$downs ▼",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DownColor,
                    )
                }
                Text(
                    text = "öne çıkanlar",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

/** Şirket satırı: hisse + isim + favori yıldızı. */
@Composable
private fun CompanyRow(
    company: Ticker,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onClick: (String) -> Unit,
) {
    val ticker = company.ticker ?: return
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple { onClick(ticker) }
                .padding(start = 14.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ticker.take(2),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticker,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                (company.companyName ?: company.name)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = { onToggleFavorite(ticker) }) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        TextSecondary.copy(alpha = 0.35f)
                    },
                )
            }
        }
    }
}

/** Kısa süreli yükleme iskeletleri. */
@Composable
private fun DashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.width(148.dp).height(92.dp))
            }
        }
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp))
        repeat(4) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(64.dp))
        }
    }
}

/** Fiyat değişimini info verisinden hesapla. */
fun computeChangePct(market: MarketData?): Double? {
    val price = market?.currentPrice ?: return null
    val prev = market?.previousClose ?: return null
    if (prev == 0.0) return null
    return (price - prev) / prev * 100
}
