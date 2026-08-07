package com.florence.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.R
import com.florence.app.core.theme.TextSecondary
import com.florence.app.presentation.components.LogoMark

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0E1A33), MaterialTheme.colorScheme.background),
                    startY = 0f, endY = 1400f,
                ),
            ),
    ) {
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
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.auth_register),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuthField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.auth_username),
                    icon = { Icon(Icons.Filled.Person, null, tint = TextSecondary) },
                    imeAction = ImeAction.Next,
                )
                AuthField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.auth_email),
                    icon = { Icon(Icons.Filled.Email, null, tint = TextSecondary) },
                    imeAction = ImeAction.Next,
                    isEmail = true,
                )
                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.auth_password),
                    icon = { Icon(Icons.Filled.Lock, null, tint = TextSecondary) },
                    imeAction = ImeAction.Next,
                    isPassword = true,
                )
                AuthField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = stringResource(R.string.auth_confirm_password),
                    icon = { Icon(Icons.Filled.Lock, null, tint = TextSecondary) },
                    imeAction = ImeAction.Done,
                    isPassword = true,
                )

                val errorRes = validationError ?: uiState.errorRes
                errorRes?.let { res ->
                    Text(
                        text = stringResource(res),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                GradientButton(
                    text = stringResource(R.string.auth_register),
                    enabled = !uiState.busy,
                    loading = uiState.busy,
                    onClick = {
                        validationError = when {
                            password.length < 10 -> R.string.auth_password_too_short
                            password != confirm -> R.string.auth_passwords_dont_match
                            else -> null
                        }
                        if (validationError == null) {
                            viewModel.register(username, email, password)
                        }
                    },
                )
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(R.string.auth_has_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
