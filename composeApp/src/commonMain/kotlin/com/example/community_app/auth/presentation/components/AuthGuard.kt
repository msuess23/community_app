package com.example.community_app.auth.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.dto.UserDto
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_guard_text
import community_app.composeapp.generated.resources.auth_guard_title
import community_app.composeapp.generated.resources.auth_login_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Lock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AuthGuard(
  onLoginClick: () -> Unit,
  modifier: Modifier = Modifier,
  fallbackContent: (@Composable () -> Unit)? = null,
  content: @Composable (UserDto) -> Unit
) {
  val authRepo = koinInject<AuthRepository>()
  val authState by authRepo.authState.collectAsStateWithLifecycle(initialValue = AuthState.Loading)

  when (val state = authState) {
    is AuthState.Loading -> {
      Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        CircularProgressIndicator()
      }
    }
    is AuthState.Unauthenticated -> {
      if (fallbackContent != null) {
        fallbackContent()
      } else {
        DefaultAuthFallback(
          onLoginClick = onLoginClick,
          modifier = modifier
        )
      }
    }
    is AuthState.Authenticated -> {
      content(state.user)
    }
  }
}

@Composable
private fun DefaultAuthFallback(
  onLoginClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = FeatherIcons.Lock,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = stringResource(Res.string.auth_guard_title),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(Res.string.auth_guard_text),
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onLoginClick) {
      Text(stringResource(Res.string.auth_login_label))
    }
  }
}