package com.example.community_app.core.presentation.helpers

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus
import com.example.community_app.util.TicketVisibility
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.category_announcement
import community_app.composeapp.generated.resources.category_cleaning
import community_app.composeapp.generated.resources.category_construction
import community_app.composeapp.generated.resources.category_event
import community_app.composeapp.generated.resources.category_infrastructure
import community_app.composeapp.generated.resources.category_maintenance
import community_app.composeapp.generated.resources.category_noise
import community_app.composeapp.generated.resources.category_other
import community_app.composeapp.generated.resources.category_safety
import community_app.composeapp.generated.resources.status_active
import community_app.composeapp.generated.resources.status_cancelled
import community_app.composeapp.generated.resources.status_done
import community_app.composeapp.generated.resources.status_in_progress
import community_app.composeapp.generated.resources.status_open
import community_app.composeapp.generated.resources.status_rejected
import community_app.composeapp.generated.resources.status_resolved
import community_app.composeapp.generated.resources.status_scheduled
import community_app.composeapp.generated.resources.ticket_visibility_private
import community_app.composeapp.generated.resources.ticket_visibility_public
import community_app.composeapp.generated.resources.welcome_back

fun InfoCategory.toUiText(): UiText {
  val res = when(this) {
    InfoCategory.EVENT -> Res.string.category_event
    InfoCategory.CONSTRUCTION -> Res.string.category_construction
    InfoCategory.MAINTENANCE -> Res.string.category_maintenance
    InfoCategory.ANNOUNCEMENT -> Res.string.category_announcement
    InfoCategory.OTHER -> Res.string.category_other
  }
  return UiText.StringResourceId(res)
}

fun InfoStatus.toUiText(): UiText {
  val res = when(this) {
    InfoStatus.SCHEDULED -> Res.string.status_scheduled
    InfoStatus.ACTIVE -> Res.string.status_active
    InfoStatus.DONE -> Res.string.status_done
    InfoStatus.CANCELLED -> Res.string.status_cancelled
  }
  return UiText.StringResourceId(res)
}

fun TicketCategory.toUiText(): UiText {
  val res = when(this) {
    TicketCategory.INFRASTRUCTURE -> Res.string.category_infrastructure
    TicketCategory.CLEANING -> Res.string.category_cleaning
    TicketCategory.SAFETY -> Res.string.category_safety
    TicketCategory.NOISE -> Res.string.category_noise
    TicketCategory.OTHER -> Res.string.category_other
  }
  return UiText.StringResourceId(res)
}

fun TicketStatus.toUiText(): UiText {
  val res = when(this) {
    TicketStatus.OPEN -> Res.string.status_open
    TicketStatus.IN_PROGRESS -> Res.string.status_in_progress
    TicketStatus.RESOLVED -> Res.string.status_resolved
    TicketStatus.CANCELLED -> Res.string.status_cancelled
    TicketStatus.REJECTED -> Res.string.status_rejected
  }
  return UiText.StringResourceId(res)
}

fun TicketVisibility.toUiText(): UiText {
  val res = when(this) {
    TicketVisibility.PUBLIC -> Res.string.ticket_visibility_public
    TicketVisibility.PRIVATE -> Res.string.ticket_visibility_private
  }
  return UiText.StringResourceId(res)
}