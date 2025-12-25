package com.example.community_app.appointment.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.appointment.presentation.detail.component.AppointmentOfficeCard
import com.example.community_app.appointment.presentation.detail.component.NoteEditDialog
import com.example.community_app.appointment.presentation.detail.component.NotesSection
import com.example.community_app.appointment.presentation.detail.component.TimeCard
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.detail.CommunityAddressCard
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_cancel_text
import community_app.composeapp.generated.resources.appointment_cancel_title
import community_app.composeapp.generated.resources.appointment_singular
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.no
import community_app.composeapp.generated.resources.yes
import compose.icons.FeatherIcons
import compose.icons.feathericons.Trash2
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppointmentDetailScreenRoot(
  viewModel: AppointmentDetailViewModel = koinViewModel(),
  onNavigateToOffice: (Int) -> Unit,
  onNavigateBack: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.isCancelSuccess) {
    if (state.isCancelSuccess) {
      onNavigateBack()
    }
  }

  AppointmentDetailScreen(
    state = state,
    onAction = { action ->
      when(action) {
        is AppointmentDetailAction.OnOfficeClick -> onNavigateToOffice(action.officeId)
        is AppointmentDetailAction.OnNavigateBack -> onNavigateBack()
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
  val snackbarHostState = remember { SnackbarHostState() }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = (state.isLoading && state.appointment == null)
  )

  DetailScreenLayout(
    title = stringResource(Res.string.appointment_singular),
    onNavigateBack = { onAction(AppointmentDetailAction.OnNavigateBack) },
    isLoading = state.isLoading,
    dataAvailable = state.appointment != null,
    actions = {
      if (!state.isCancelling && !state.isLoading) {
        IconButton(onClick = { onAction(AppointmentDetailAction.OnCancelClick) }) {
          Icon(
            imageVector = FeatherIcons.Trash2,
            contentDescription = stringResource(Res.string.cancel),
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    },
    snackbarHostState = snackbarHostState
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(Spacing.medium),
      verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
      val appointment = state.appointment ?: return@DetailScreenLayout
      val office = state.office

      TimeCard(appointment)

      // Office
      if (office != null) {
        AppointmentOfficeCard(
          office = office,
          onClick = { onAction(AppointmentDetailAction.OnOfficeClick(office.id)) }
        )
        CommunityAddressCard(address = office.address)
      }

      NotesSection(
        notes = state.notes,
        onAddClick = { onAction(AppointmentDetailAction.OnAddNoteClick) },
        onEditClick = { onAction(AppointmentDetailAction.OnEditNoteClick(it)) },
        onDeleteClick = { onAction(AppointmentDetailAction.OnDeleteNoteClick(it.id)) }
      )
    }
  }

  if (state.showCancelDialog) {
    CommunityDialog(
      title = Res.string.appointment_cancel_title,
      text = Res.string.appointment_cancel_text,
      onDismissRequest = { onAction(AppointmentDetailAction.OnDismissDialog) },
      confirmButtonText = Res.string.yes,
      onConfirm = { onAction(AppointmentDetailAction.OnCancelConfirm) },
      dismissButtonText = Res.string.no,
      onDismiss = { onAction(AppointmentDetailAction.OnDismissDialog) }
    )
  }

  if (state.isNoteDialogVisible) {
    NoteEditDialog(
      initialText = state.editingNote?.text ?: "",
      isEdit = state.editingNote != null,
      onDismiss = { onAction(AppointmentDetailAction.OnCloseNoteDialog) },
      onConfirm = { text -> onAction(AppointmentDetailAction.OnSubmitNote(text)) }
    )
  }
}