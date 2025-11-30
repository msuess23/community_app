package com.example.community_app.office.presentation.office_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.office.domain.Office
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
            onPrev = { onAction(OfficeDetailAction.OnPreviousDayClick) },
            onNext = { onAction(OfficeDetailAction.OnNextDayClick) }
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
    CommunityDialog(
      title = Res.string.appointment_singular,
      text = Res.string.appointment_singular, // TODO
      onDismissRequest = { onAction(OfficeDetailAction.OnDismissBookingDialog) },
      confirmButtonText = Res.string.save, // TODO
      onConfirm = { onAction(OfficeDetailAction.OnConfirmBooking) },
      dismissButtonText = Res.string.cancel,
      onDismiss = { onAction(OfficeDetailAction.OnDismissBookingDialog) }
    )
  }
}

@Composable
private fun OfficeHeader(office: Office) {
  Column(modifier = Modifier.padding(Spacing.medium)) {
    if (!office.description.isNullOrBlank()) {
      Text(
        text = office.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(Spacing.small))
    }

    // Address
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = FeatherIcons.MapPin,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.width(16.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "${office.address.street} ${office.address.houseNumber}, ${office.address.city}",
        style = MaterialTheme.typography.bodySmall
      )
    }

    // Phone
    if (!office.phone.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = FeatherIcons.Phone,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.width(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = office.phone, style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

