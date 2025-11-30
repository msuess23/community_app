package com.example.community_app.ticket.presentation.ticket_detail

sealed interface TicketDetailAction {
  data object OnNavigateBack : TicketDetailAction
  data object OnEditClick : TicketDetailAction
  data object OnShowStatusHistory : TicketDetailAction
  data object OnDismissStatusHistory : TicketDetailAction
}