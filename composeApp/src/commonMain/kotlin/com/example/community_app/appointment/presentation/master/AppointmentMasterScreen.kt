package com.example.community_app.appointment.presentation.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.appointment.presentation.master.component.AppointmentListItem
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.components.master.MasterScreenLayout
import com.example.community_app.core.presentation.components.master.SingleTabHeader
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_none
import community_app.composeapp.generated.resources.appointment_plural
import community_app.composeapp.generated.resources.search_no_results
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppointmentMasterScreenRoot(
  viewModel: AppointmentMasterViewModel = koinViewModel(),
  onNavigateToDetail: (Int) -> Unit,
  onNavigateToLogin: () -> Unit,
  onOpenDrawer: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  AppointmentMasterScreen(
    state = state,
    onAction = { action ->
      when(action) {
        is AppointmentMasterAction.OnAppointmentClick -> onNavigateToDetail(action.appointment.id)
        AppointmentMasterAction.OnLoginClick -> onNavigateToLogin()
        else -> viewModel.onAction(action)
      }
    },
    onOpenDrawer = onOpenDrawer
  )
}

@Composable
private fun AppointmentMasterScreen(
  state: AppointmentMasterState,
  onAction: (AppointmentMasterAction) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  MasterScreenLayout(
    searchQuery = "",
    isFilterActive = false,
    isLoading = state.isLoading,
    isEmpty = state.appointments.isEmpty(),
    snackbarHostState = snackbarHostState,
    onSearchQueryChange = {},
    onRefresh = { onAction(AppointmentMasterAction.OnRefresh) },
    onOpenDrawer = onOpenDrawer,
    onToggleFilterSheet = {},
    tabsContent = { SingleTabHeader(Res.string.appointment_plural) },
    emptyStateContent = {
      if (!state.isUserLoggedIn) {
        AuthGuard(
          onLoginClick = { onAction(AppointmentMasterAction.OnLoginClick) },
          content = { }
        )
      } else {
        ScreenMessage(
          text = stringResource(Res.string.appointment_none),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      items(state.appointments, key = { it.id }) { appointment ->
        AppointmentListItem(
          appointment = appointment,
          onClick = { onAction(AppointmentMasterAction.OnAppointmentClick(appointment)) }
        )
      }
    }
  }
}