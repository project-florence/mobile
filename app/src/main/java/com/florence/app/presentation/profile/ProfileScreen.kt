package com.florence.app.presentation.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.BuildConfig
import com.florence.app.R
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard
import com.florence.app.presentation.settings.CreditsViewModel
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Saate göre selamlama anahtarı: 05-11 Günaydın, 12-17 İyi günler, 18-22 İyi akşamlar, 23-04 İyi geceler. */
fun greetingResFor(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..17 -> R.string.greeting_day
    in 18..22 -> R.string.greeting_evening
    else -> R.string.greeting_night
}

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    creditsViewModel: CreditsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val creditsState by creditsViewModel.uiState.collectAsStateWithLifecycle()

    val profile = creditsState.profile
    val username = profile?.username ?: "Yatırımcı"

    // Canlı saat — her 30 saniyede bir güncellenir.
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(30_000)
        }
    }
    val greeting = stringResource(greetingResFor(now.hour), username)
    val clock = now.format(DateTimeFormatter.ofPattern("HH:mm"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ---- Selamlama başlığı ----
        item {
            ProfileHeader(
                username = username,
                email = profile?.email,
                isAdmin = creditsState.isAdmin,
                greeting = greeting,
                clock = clock,
            )
        }

        // ---- Coin kartı ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        FlorencePalettes.Florence.primary,
                                        FlorencePalettes.Florence.secondary,
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.size(14.dp))
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    IconButton(onClick = { creditsViewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile")
                    }
                }
            }
        }

        // ---- Admin Paneli (şimdilik pasif — yakında) ----
        item {
            FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FlorencePalettes.Florence.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = FlorencePalettes.Florence.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Paneli",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Text(
                            text = "Hesap ve coin yönetimi",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FlorencePalettes.Florence.secondary.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.coming_soon_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FlorencePalettes.Florence.secondary,
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.profile_announcements),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (uiState.loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (uiState.announcements.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.profile_no_announcements),
                    subtitle = stringResource(R.string.profile_no_announcements_hint),
                )
            }
        } else {
            items(uiState.announcements, key = { it.id ?: it.hashCode() }) { ann ->
                FlorenceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = FlorencePalettes.Florence.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = ann.title ?: "Duyuru",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        ann.body?.let {
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        ann.created_at?.let {
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = it.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.size(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Florence v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.size(8.dp))
            OutlinedButton(
                onClick = { viewModel.logout(onLoggedOut) },
                enabled = !uiState.loggingOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DownColor),
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(if (uiState.loggingOut) stringResource(R.string.profile_logging_out) else stringResource(R.string.profile_logout))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    email: String?,
    isAdmin: Boolean,
    greeting: String,
    clock: String,
) {
    val primary = FlorencePalettes.Florence.primary
    val secondary = FlorencePalettes.Florence.secondary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(primary.copy(alpha = 0.30f), MaterialTheme.colorScheme.background),
                )
            )
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Büyük avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(primary, secondary))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = clock,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                    )
                    if (isAdmin) {
                        Spacer(Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(UpColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UpColor,
                            )
                        }
                    }
                }
            }
        }
        email?.let {
            Spacer(Modifier.size(10.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
