package com.florence.app.presentation.ipo

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.TextSecondary
import com.florence.app.data.model.IpoDetail
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard

@Composable
fun IpoDetailScreen(
    viewModel: IpoDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                TextButton(onClick = { viewModel.load() }) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
        uiState.detail == null -> {
            EmptyState(
                title = stringResource(R.string.ipo_not_found),
                subtitle = stringResource(R.string.ipo_empty_subtitle),
            )
        }
        else -> {
            IpoDetailContent(uiState.detail!!)
        }
    }
}

@Composable
private fun IpoDetailContent(detail: IpoDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ---- Başlık ----
        item {
            Column {
                detail.companyName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                detail.ticker?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.ipo_ticker, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                detail.updatedAt?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.ipo_updated, it.take(16)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        // ---- Şirket bilgisi ----
        detail.company?.takeIf {
            !it.city.isNullOrBlank() || !it.founded.isNullOrBlank() || !it.description.isNullOrBlank()
        }?.let { company ->
            item {
                SectionCard(titleRes = R.string.ipo_company) {
                    company.city?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(label = stringResource(R.string.ipo_city, it))
                    }
                    company.founded?.takeIf { it.isNotBlank() }?.let {
                        InfoRow(label = stringResource(R.string.ipo_founded, it))
                    }
                    company.description?.takeIf { it.isNotBlank() }?.let {
                        Row(Modifier.padding(top = 2.dp)) { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }

        // ---- Bilgiler (key→value) ----
        if (detail.info.isNotEmpty()) {
            item {
                SectionCard(titleRes = R.string.ipo_info) {
                    detail.info.entries.forEachIndexed { index, (key, value) ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Row(Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        // ---- Detay bölümleri (başlık→paragraf) ----
        detail.sections.entries.forEach { (title, body) ->
            item {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(titleRes: Int, content: @Composable () -> Unit) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(6.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
}