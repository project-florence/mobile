package com.florence.app.presentation.economy

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.florence.app.data.model.MacroeconomyResponse
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import java.util.Locale

/** Altın türü anahtarı → ekranda gösterilecek güzel ad. */
private val GOLD_DISPLAY = mapOf(
    "gram-altin" to "Gram Altın",
    "ceyrek-altin" to "Çeyrek Altın",
    "ons" to "Ons",
    "gram-has-altin" to "Gram Has Altın",
    "yarim-altin" to "Yarım Altın",
    "tam-altin" to "Tam Altın",
    "cumhuriyet-altini" to "Cumhuriyet Altını",
    "ata-altin" to "Ata Altın",
    "14-ayar-altin" to "14 Ayar Altın",
    "18-ayar-altin" to "18 Ayar Altın",
    "22-ayar-bilezik" to "22 Ayar Bilezik",
    "ikibucuk-altin" to "İkibuçuk Altın",
    "besli-altin" to "Beşli Altın",
    "gremse-altin" to "Gremse Altın",
    "resat-altin" to "Reşat Altın",
    "hamit-altin" to "Hamit Altın",
)

@Composable
fun EconomyScreen(
    viewModel: EconomyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        uiState.currency.isEmpty() -> {
            EmptyState(
                title = "Döviz verisi bekleniyor",
                subtitle = "Kur verisi sağlayıcısı şu an yanıt vermiyor. Kısa süre sonra tekrar dene.",
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "Döviz Kurları",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Güncel kur bilgileri",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(uiState.currency.size) { index ->
                    val (symbol, quote) = uiState.currency.entries.elementAt(index)
                    CurrencyRow(symbol, quote)
                }

                // ---- Metaller bölümü ----
                item {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Metaller",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Altın, gümüş, platin ve paladyum fiyatları",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(uiState.gold.size) { index ->
                    val (key, quote) = uiState.gold.entries.elementAt(index)
                    CurrencyRow(GOLD_DISPLAY[key] ?: key, quote)
                }
                uiState.silver?.let { silver ->
                    item { CurrencyRow("Gümüş (gram)", silver) }
                }
                uiState.platinum?.let { platinum ->
                    item { CurrencyRow("Platin (ons)", platinum) }
                }
                uiState.palladium?.let { palladium ->
                    item { CurrencyRow("Paladyum (ons)", palladium) }
                }

                // ---- Makroekonomi bölümü ----
                item {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.macro_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.macro_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (uiState.macroLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    val m = uiState.macro
                    if (uiState.macroError || m == null) {
                        item {
                            EmptyState(
                                title = stringResource(R.string.macro_error),
                                subtitle = stringResource(R.string.macro_error_hint),
                            )
                        }
                    } else {
                        item { MacroMetricRow(stringResource(R.string.macro_fed_funds), m.fedFundsRate ?: m.fedFunds, "%") }
                        item { MacroMetricRow(stringResource(R.string.macro_us_unrate), m.usaUnrate, "%") }
                        item { MacroMetricRow(stringResource(R.string.macro_vix), m.vix, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_sp500), m.sp500, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_nasdaq), m.nasdaq, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_bitcoin), m.bitcoin, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_brent), m.brentCrudeOilPrice, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_wti), m.wtiCrudeOilPrice, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_dxy), m.dxy, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_10y), m.usa10yTreasury, "%") }
                        item { MacroMetricRow(stringResource(R.string.macro_gdp), m.usaGdp, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_real_gdp), m.usaRealGdp, "") }
                        item { MacroMetricRow(stringResource(R.string.macro_cpi), m.usaConsumerCpi, "") }
                    }
                }
            }
        }
    }
}

/** Makroekonomi metriği: solda etiket, sağda değer (null ise "--"). */
@Composable
private fun MacroMetricRow(label: String, value: Double?, suffix: String) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (value != null) {
                    val formatted = String.format(Locale.US, "%.2f", value)
                    if (suffix.isNotEmpty()) "$formatted$suffix" else formatted
                } else {
                    "--"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (value != null) MaterialTheme.colorScheme.onSurface else TextSecondary,
            )
        }
    }
}

@Composable
private fun CurrencyRow(symbol: String, quote: com.florence.app.data.model.CurrencyQuote) {
    FlorenceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = quote.buying?.let { "Alış $it" } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = quote.selling ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                quote.change?.let { change ->
                    val up = change.startsWith("%+")
                    Text(
                        text = change,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (up) com.florence.app.core.theme.UpColor else com.florence.app.core.theme.DownColor,
                    )
                }
            }
        }
    }
}
