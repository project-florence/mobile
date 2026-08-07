package com.florence.app.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard

@Composable
fun ReportDetailScreen(
    onBack: () -> Unit,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val report = uiState.report

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.loading && report == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (report == null) {
            EmptyState(
                title = uiState.error ?: "Rapor yüklenemedi",
                subtitle = "Lütfen tekrar dene.",
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Başlık
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.title ?: "AI Raporu",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${report.ticker ?: ""} · ${report.date?.take(10) ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        IconButton(onClick = viewModel::load) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                        }
                    }
                }

                // Özet
                report.about?.takeIf { it.isNotBlank() }?.let { about ->
                    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Özet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FlorencePalettes.Florence.primary,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = about,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                // Rapor metni
                report.report?.takeIf { it.isNotBlank() }?.let { body ->
                    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Analiz",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FlorencePalettes.Florence.primary,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } ?: run {
                    EmptyState(
                        title = "Rapor içeriği yok",
                        subtitle = "Rapor henüz hazırlanmamış olabilir.",
                    )
                }

                // Kaynak analizleri
                if (report.sentiments.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(
                        text = "Kaynak Analizleri (${report.sentiments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    report.sentiments.forEach { s ->
                        FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = s.title ?: s.source ?: "Kaynak",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    s.sentiment?.let { sent ->
                                        val color = when (sent.lowercase()) {
                                            "positive", "pozitif" -> com.florence.app.core.theme.UpColor
                                            "negative", "negatif" -> com.florence.app.core.theme.DownColor
                                            else -> TextSecondary
                                        }
                                        Text(
                                            text = sent,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = color,
                                        )
                                    }
                                }
                                s.summary?.takeIf { it.isNotBlank() }?.let { sum ->
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        text = sum,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                report.tokenUsage?.let { usage ->
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Token: ${usage["total"] ?: 0} (giriş ${usage["prompt"] ?: 0} / çıkış ${usage["completion"] ?: 0})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
