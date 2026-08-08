package com.florence.app.presentation.legal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.TextSecondary
import com.florence.app.data.model.LegalResponse
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LegalDetailViewModel @Inject constructor(
    private val repo: MiscRepository,
) : ViewModel() {

    data class LegalUiState(
        val loading: Boolean = true,
        val content: String? = null,
        val lastUpdated: String? = null,
        val error: Boolean = false,
    )

    private val _uiState = MutableStateFlow(LegalUiState())
    val uiState: StateFlow<LegalUiState> = _uiState.asStateFlow()

    fun load(policy: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            val lang = if (Locale.getDefault().language == "en") "en" else "tr"
            val result = repo.legal(policy, lang)
            _uiState.update {
                it.copy(
                    loading = false,
                    content = result.getOrNull()?.content,
                    lastUpdated = result.getOrNull()?.lastUpdated,
                    error = result.isFailure,
                )
            }
        }
    }
}

@Composable
fun LegalDetailScreen(
    policy: String,
    viewModel: LegalDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val titleRes = LEGAL_ENTRIES.firstOrNull { it.policy == policy }?.titleRes ?: R.string.legal_terms

    LaunchedEffect(policy) { viewModel.load(policy) }

    if (uiState.loading && uiState.content == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        uiState.lastUpdated?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Son güncelleme: $it",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(16.dp))
        when {
            uiState.error -> Text(
                text = "Belge yüklenemedi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )

            uiState.content.isNullOrBlank() -> Text(
                text = "Belge henüz eklenmedi.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            else -> Text(
                text = uiState.content.orEmpty().trim(),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.35f,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
