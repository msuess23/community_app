package com.example.community_app.office.presentation.office_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.detail.CommunityAddressCard
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.detail.MapPlaceholder
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.office.domain.Office
import com.example.community_app.office.presentation.office_detail.component.CommunityDatePicker
import com.example.community_app.office.presentation.office_detail.component.DateSelector
import com.example.community_app.office.presentation.office_detail.component.SlotItem
import compose.icons.FeatherIcons
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Phone
import org.koin.compose.viewmodel.koinViewModel
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_singular
import community_app.composeapp.generated.resources.save
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.office_singular
import compose.icons.feathericons.Clock
import compose.icons.feathericons.Mail
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfficeDetailScreenRoot(
  viewModel: OfficeDetailViewModel = koinViewModel(),
  onNavigateBack: () -> Unit,
  onNavigateToLogin: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  OfficeDetailScreen(
    state = state,
    onAction = { action ->
      when(action) {
        OfficeDetailAction.OnNavigateBack -> onNavigateBack()
        OfficeDetailAction.OnLoginRedirect -> onNavigateToLogin()
        else -> viewModel.onAction(action)
      }
    }
  )
}

@Composable
private fun OfficeDetailScreen(
  state: OfficeDetailState,
  onAction: (OfficeDetailAction) -> Unit
) {
  DetailScreenLayout(
    title = state.office?.name ?: stringResource(Res.string.office_singular),
    onNavigateBack = { onAction(OfficeDetailAction.OnNavigateBack) },
    isLoading = state.isLoading,
    dataAvailable = state.office != null
  ) {
    state.office?.let { office ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.extraLarge)
      ) {
        // Office Info Header
        item {
          OfficeHeader(office)
        }

        item {
          HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))
        }

        // Date Selector
        item {
          DateSelector(
            dateMillis = state.selectedDateMillis,
            dateRange = state.selectableDateRange,
            onPrev = { onAction(OfficeDetailAction.OnPreviousDayClick) },
            onNext = { onAction(OfficeDetailAction.OnNextDayClick) },
            onCalendarClick = { onAction(OfficeDetailAction.OnCalendarClick) }
          )
        }

        // Slots List
        if (state.isLoadingSlots) {
          item {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
          }
        } else if (state.visibleSlots.isEmpty()) {
          item {
            Text(
              text = "Keine freien Termine an diesem Tag.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth().padding(Spacing.medium)
            )
          }
        } else {
          items(state.visibleSlots) { slot ->
            SlotItem(
              slot = slot,
              onClick = { onAction(OfficeDetailAction.OnSlotClick(slot)) }
            )
          }
        }
      }
    }
  }

  // Booking Dialog
  if (state.selectedSlot != null) {
//    CommunityDialog(
//      title = Res.string.appointment_singular,
//      text = Res.string.appointment_singular, // TODO
//      onDismissRequest = { onAction(OfficeDetailAction.OnDismissBookingDialog) },
//      confirmButtonText = Res.string.save, // TODO
//      onConfirm = { onAction(OfficeDetailAction.OnConfirmBooking) },
//      dismissButtonText = Res.string.cancel,
//      onDismiss = { onAction(OfficeDetailAction.OnDismissBookingDialog) }
//    )

    AlertDialog(
      onDismissRequest = { onAction(OfficeDetailAction.OnDismissBookingDialog) },
      title = { Text(stringResource(Res.string.appointment_singular)) },
      text = {
        Column {
          Text("Möchtest Du diesen Termin verbindlich buchen?")

          if (state.hasCalendarPermission) {
            Spacer(Modifier.height(16.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable {
                onAction(OfficeDetailAction.OnToggleCalendarExport(!state.shouldAddToCalendar))
              }
            ) {
              Checkbox(
                checked = state.shouldAddToCalendar,
                onCheckedChange = { onAction(OfficeDetailAction.OnToggleCalendarExport(it)) }
              )
              Text("In Kalender exportieren")
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { onAction(OfficeDetailAction.OnConfirmBooking) }) {
          Text(stringResource(Res.string.save))
        }
      },
      dismissButton = {
        TextButton(onClick = { onAction(OfficeDetailAction.OnDismissBookingDialog) }) {
          Text(stringResource(Res.string.cancel))
        }
      }
    )
  }

  if (state.showDatePicker) {
    CommunityDatePicker(
      initialDateMillis = state.selectedDateMillis,
      dateRange = state.selectableDateRange,
      onDateSelected = { millis -> onAction(OfficeDetailAction.OnDateSelected(millis)) },
      onDismiss = { onAction(OfficeDetailAction.OnDismissDatePicker) }
    )
  }
}

@Composable
private fun OfficeHeader(office: Office) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    modifier = Modifier.padding(Spacing.medium)
  ) {
    if (!office.description.isNullOrBlank()) {
      Text(
        text = office.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    if (!office.services.isNullOrBlank()) {
      Text(
        text = office.services,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    if (!office.openingHours.isNullOrBlank()) {
      Row(verticalAlignment = Alignment.Top) {
        Icon(FeatherIcons.Clock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(16.dp))
        Column {
          Text(
            text = "Öffnungszeiten", // TODO: Localize
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = office.openingHours,
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }

    // Contact Box
    if (!office.phone.isNullOrBlank() || !office.contactEmail.isNullOrBlank()) {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          if (!office.phone.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(FeatherIcons.Phone, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(8.dp))
              Text(office.phone, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
          }

          if (!office.contactEmail.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(FeatherIcons.Mail, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(8.dp))
              Text(office.contactEmail, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    }

    // Address
    CommunityAddressCard(address = office.address)
  }
}

