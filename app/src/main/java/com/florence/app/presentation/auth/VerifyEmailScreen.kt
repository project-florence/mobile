package com.florence.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.TextSecondary
import com.florence.app.presentation.components.LogoMark
import com.florence.app.presentation.components.ShaderBackground
import kotlinx.coroutines.delay

/** 429 sonrası "yeniden gönder" için kısa geri sayım (saniye). */
private const val RATE_LIMIT_COOLDOWN_SECONDS = 60

/**
 * E-posta doğrulama ekranı. Üç yoldan ulaşılır:
 *  - kayıt sonrası (verification_sent false ise otomatik "yeniden gönder")
 *  - login 403 'error_email_not_verified'
 *  - refresh 403 'error_email_not_verified' (oturum kapanınca, target boş)
 */
@Composable
fun VerifyEmailScreen(
    onBackToLogin: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var cooldown by rememberSaveable { mutableStateOf(0) }
    var autoResent by rememberSaveable { mutableStateOf(false) }

    // verification_sent=false olduğunda tek seferlik otomatik "yeniden gönder".
    LaunchedEffect(uiState.verifyAutoResend) {
        if (uiState.verifyAutoResend && !autoResent) {
            autoResent = true
            viewModel.resendVerification()
        }
    }

    // 429 gelince geri sayımı başlat ve butonu bir süre devre dışı bırak.
    LaunchedEffect(uiState.verifyErrorRes) {
        if (uiState.verifyErrorRes == R.string.verify_rate_limited) {
            cooldown = RATE_LIMIT_COOLDOWN_SECONDS
        }
    }
    LaunchedEffect(cooldown) {
        while (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    ShaderBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LogoMark(size = 56.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.verify_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.verify_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            if (uiState.verifyTarget.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = uiState.verifyTarget,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.verifyMessageRes?.let { res ->
                    Text(
                        text = stringResource(res),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                uiState.verifyErrorRes?.let { res ->
                    Text(
                        text = stringResource(res),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (cooldown > 0) {
                    Text(
                        text = stringResource(R.string.verify_cooldown, cooldown),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                GradientButton(
                    text = stringResource(R.string.verify_resend),
                    enabled = !uiState.verifyBusy && cooldown == 0 && uiState.verifyTarget.isNotBlank(),
                    loading = uiState.verifyBusy,
                    onClick = { viewModel.resendVerification() },
                )
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.verify_continue),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onLoggedOut) {
                Text(
                    text = stringResource(R.string.verify_back_to_login),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
