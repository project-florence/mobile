package com.florence.app.presentation.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florence.app.R
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.TextSecondary

/** Web'deki yasal başlıklar — backend policy anahtarlarıyla birebir. */
data class LegalEntry(
    val policy: String,
    val titleRes: Int,
)

val LEGAL_ENTRIES = listOf(
    LegalEntry("terms", R.string.legal_terms),
    LegalEntry("privacy_policy", R.string.legal_privacy),
    LegalEntry("cookie_policy", R.string.legal_cookies),
    LegalEntry("disclaimer", R.string.legal_disclaimer),
)

@Composable
fun LegalScreen(onOpenPolicy: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "Platformumuzu kullanırken geçerli olan resmi belgeler:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(LEGAL_ENTRIES.size) { index ->
            val entry = LEGAL_ENTRIES[index]
            Card(
                onClick = { onOpenPolicy(entry.policy) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(entry.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = FlorencePalettes.Florence.primary,
                    )
                }
            }
        }
    }
}
