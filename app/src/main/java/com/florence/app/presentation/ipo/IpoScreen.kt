package com.florence.app.presentation.ipo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.data.model.IpoItem
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard

private fun IpoTab.labelRes(): Int = when (this) {
    IpoTab.UPCOMING -> R.string.ipo_tab_upcoming
    IpoTab.ACTIVE -> R.string.ipo_tab_active
    IpoTab.DRAFT -> R.string.ipo_tab_draft
}

@Composable
fun IpoScreen(
    onOpenIpo: (String) -> Unit,
    viewModel: IpoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // ---- Başlık ----
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.ipo_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.ipo_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ---- Sekmeler: Yaklaşan / Aktif / Taslak ----
        TabRow(selectedTabIndex = uiState.currentTab.ordinal) {
            IpoTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.currentTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(stringResource(tab.labelRes())) },
                )
            }
        }

        // ---- İçerik ----
        when {
            uiState.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.errorRes != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(uiState.errorRes!!),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.size(12.dp))
                    TextButton(onClick = { viewModel.retry() }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }
            uiState.currentList.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.ipo_empty_title),
                    subtitle = stringResource(R.string.ipo_empty_subtitle),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.currentList, key = { it.id ?: it.hashCode() }) { ipo ->
                        IpoRow(
                            ipo = ipo,
                            onOpen = { ipo.slug?.let(onOpenIpo) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IpoRow(ipo: IpoItem, onOpen: () -> Unit) {
    val context = LocalContext.current
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(16.dp),
        ) {
            Text(
                text = ipo.title ?: "—",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ipo.date?.let {
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.ipo_date, it.take(10)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ipo.link?.let { link ->
                Spacer(Modifier.size(10.dp))
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.ipo_apply_link))
                }
            }
        }
    }
}