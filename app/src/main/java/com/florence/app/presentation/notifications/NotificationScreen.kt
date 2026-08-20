package com.florence.app.presentation.notifications

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.AnnouncementItem
import com.florence.app.presentation.components.EmptyState
import com.florence.app.presentation.components.FlorenceCard

/**
 * Bildirim paneli — backend /announcements ile dolu gerçek duyuru listesi.
 * Okundu işaretlemesi yalnızca GÖRSEL ve oturum-içidir: backend'de kullanıcı
 * tarafı okundu ucu yok (admin-only), bu yüzden isUnread'e göre gösterilir ve
 * kart tıklanınca yerelde okundu yapılır (kalıcı değil).
 */
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        uiState.error -> {
            Box(modifier = Modifier.fillMaxSize()) {
                EmptyState(
                    title = stringResource(R.string.notifications_error),
                    subtitle = stringResource(R.string.notifications_error_hint),
                )
            }
        }
        uiState.announcements.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize()) {
                EmptyState(
                    title = stringResource(R.string.notifications_empty),
                    subtitle = stringResource(R.string.notifications_empty_hint),
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.announcements, key = { it.id ?: it.hashCode() }) { ann ->
                    AnnouncementCard(
                        item = ann,
                        isRead = uiState.isRead(ann),
                        onClick = { viewModel.markRead(ann) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(
    item: AnnouncementItem,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    FlorenceCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = item.title ?: stringResource(R.string.notifications_announcement),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (!isRead) {
                    Spacer(Modifier.size(8.dp))
                    UnreadBadge()
                }
            }
            item.content?.let {
                Spacer(Modifier.size(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.createdAt?.let { created ->
                Spacer(Modifier.size(6.dp))
                Text(
                    text = created.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Okunmamış duyuru için vurgu rozeti (görsel, oturum-içi). */
@Composable
private fun UnreadBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(UpColor, RoundedCornerShape(4.dp))
            .size(8.dp),
    )
}
