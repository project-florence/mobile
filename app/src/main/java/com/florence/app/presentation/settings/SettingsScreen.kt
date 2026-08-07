package com.florence.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.core.settings.SettingsRepository
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.presentation.components.FlorenceCard
import android.app.Activity
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    creditsViewModel: CreditsViewModel = hiltViewModel(),
) {
    val lang by mainViewModel.lang.collectAsStateWithLifecycle()
    val theme by mainViewModel.theme.collectAsStateWithLifecycle()
    val creditsState by creditsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        // ---- Dil ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsRepository.LANGUAGES.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.setLang(key)
                                    activity?.recreate()
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = lang == key,
                                onClick = {
                                    mainViewModel.setLang(key)
                                    activity?.recreate()
                                },
                            )
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Text(
                        text = "Uygulama varsayılan olarak Türkçe açılır.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        // ---- Tema ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Web ile aynı paletler — anında uygulanır.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(10.dp))
                    SettingsRepository.THEMES.forEach { (key, label) ->
                        val palette = FlorencePalettes.all.firstOrNull { it.name.lowercase() == key }
                        ThemeRow(
                            key = key,
                            label = label,
                            selected = theme == key,
                            palette = palette,
                            onClick = { mainViewModel.setTheme(key) },
                        )
                    }
                }
            }
        }

        // ---- Kredi (coin) ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Coin Bakiyesi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Raporlar ve simülasyonlar coin harcar",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                    Text(
                        text = creditsState.credits?.let { "%.1f".format(it) } ?: "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    IconButton(onClick = { creditsViewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(
    key: String,
    label: String,
    selected: Boolean,
    palette: com.florence.app.core.theme.FlorencePalette?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (palette != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.primary)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
                )
                Spacer(Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.secondary)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
                )
                Spacer(Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.background)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}
