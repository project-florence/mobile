package com.florence.app.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.BuildConfig
import com.florence.app.R

@Composable
fun ProfileScreen(
    onGoToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.titleLarge,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE}/${BuildConfig.FLAVOR})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (session) {
            OutlinedButton(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.auth_logout))
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_guest),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.profile_sign_in_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Button(
                        onClick = onGoToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    ) {
                        Text(stringResource(R.string.auth_login))
                    }
                }
            }
        }
    }
}
