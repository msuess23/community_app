package com.example.community_app.appointment.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.detail.CommunityAddressCard
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.detail.MapPlaceholder
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatIsoTime
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_singular
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.delete
import compose.icons.FeatherIcons
import compose.icons.feathericons.Mail
import compose.icons.feathericons.Phone
import compose.icons.feathericons.Trash2
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppointmentDetailScreenRoot(
  viewModel: AppointmentDetailViewModel = koinViewModel(),
  onNavigateBack: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.isCancelled) {
    if (state.isCancelled) onNavigateBack()
  }

  AppointmentDetailScreen(
    state = state,
    onAction = { action ->
      when(action) {
        AppointmentDetailAction.OnNavigateBack -> onNavigateBack()
        else -> viewModel.onAction(action)
      }
    }
  )
}

@Composable
private fun AppointmentDetailScreen(
  state: AppointmentDetailState,
  onAction: (AppointmentDetailAction) -> Unit
) {
  DetailScreenLayout(
    title = stringResource(Res.string.appointment_singular),
    onNavigateBack = { onAction(AppointmentDetailAction.OnNavigateBack) },
    isLoading = state.isLoading,
    dataAvailable = state.appointment != null,
    actions = {
      if (!state.isCancelled && !state.isLoading) {
        IconButton(onClick = { onAction(AppointmentDetailAction.OnCancelClick) }) {
          Icon(FeatherIcons.Trash2, "Cancel", tint = MaterialTheme.colorScheme.error)
        }
      }
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(Spacing.medium)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
      val appointment = state.appointment ?: return@DetailScreenLayout
      val office = state.office

      // Time Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(Modifier.padding(Spacing.medium)) {
          Text(
            text = "Zeitpunkt", // TODO: Localize
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "${formatIsoDate(appointment.startsAt)}, ${formatIsoTime(appointment.startsAt)} - ${formatIsoTime(appointment.endsAt)}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Office Info
      if (office != null) {
        Text(
          text = "Behörde", // TODO: Localize
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = office.name,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
            )

            if (!office.phone.isNullOrBlank() || !office.contactEmail.isNullOrBlank()) {
              // Mini Kontakt Info
              Column {
                if (!office.phone.isNullOrBlank()) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(FeatherIcons.Phone, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(office.phone, style = MaterialTheme.typography.bodyMedium)
                  }
                }
                if (!office.contactEmail.isNullOrBlank()) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(FeatherIcons.Mail, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(office.contactEmail, style = MaterialTheme.typography.bodyMedium)
                  }
                }
              }
            }
          }
        }

        CommunityAddressCard(address = office.address)

        MapPlaceholder()
      }
    }
  }

  if (state.showCancelDialog) {
    CommunityDialog(
      title = Res.string.delete, // "Löschen" / "Stornieren"
      text = Res.string.cancel, // TODO: Besserer Text "Möchten Sie den Termin wirklich stornieren?"
      onDismissRequest = { onAction(AppointmentDetailAction.OnDismissDialog) },
      confirmButtonText = Res.string.delete,
      onConfirm = { onAction(AppointmentDetailAction.OnCancelConfirm) },
      dismissButtonText = Res.string.cancel,
      onDismiss = { onAction(AppointmentDetailAction.OnDismissDialog) }
    )
  }
}