package com.florence.app.presentation.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.florence.app.presentation.components.EmptyState

/**
 * Bildirim paneli — şimdilik boş; duyuru/bildirim verisi geldiğinde
 * liste burada doldurulacak (backend /announcements hazır).
 */
@Composable
fun NotificationScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        EmptyState(
            title = "Bildirim yok",
            subtitle = "Yeni bildirimler burada görünecek.",
        )
    }
}
