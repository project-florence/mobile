package com.florence.app.presentation.watchlist

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.core.theme.UpColor
import com.florence.app.core.theme.DownColor
import com.florence.app.data.repository.FavoritesRepository
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.components.TickerAvatar
import com.florence.app.presentation.components.formatPrice
import com.florence.app.presentation.home.computeChangePct
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel
class WatchlistRemoveViewModel @Inject constructor(
    private val favorites: FavoritesRepository,
) : ViewModel() {
    fun remove(ticker: String) {
        viewModelScope.launch { favorites.toggle(ticker) }
    }
}

@Composable
fun WatchlistScreen(
    onOpenCompany: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
    removeViewModel: WatchlistRemoveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.loading && uiState.items.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        uiState.items.isEmpty() -> {
            EmptyState(
                title = "İzleme listen boş",
                subtitle = "Hisse kartlarındaki yıldıza dokunarak favorilere ekleyebilirsin.",
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.items, key = { it.ticker }) { item ->
                    WatchlistRow(
                        item = item,
                        onOpen = { onOpenCompany(item.ticker) },
                        onRemove = { removeViewModel.remove(item.ticker) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistRow(
    item: WatchlistViewModel.WatchlistItem,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    val info = item.info
    val price = info?.market?.currentPrice
    val changePct = computeChangePct(info?.market)

    FlorenceCard(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TickerAvatar(ticker = item.ticker, size = 44.dp)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.ticker,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = info?.name ?: "…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (price != null) {
                    Text(
                        text = formatPrice(price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (changePct != null) {
                        Text(
                            text = if (changePct >= 0) "+%.2f%%".format(changePct) else "%.2f%%".format(changePct),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (changePct >= 0) UpColor else DownColor,
                        )
                    }
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
}
