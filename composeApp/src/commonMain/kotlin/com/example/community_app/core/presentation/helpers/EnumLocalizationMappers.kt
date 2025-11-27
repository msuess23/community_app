package com.example.community_app.core.presentation.helpers

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.category_announcement
import community_app.composeapp.generated.resources.category_construction
import community_app.composeapp.generated.resources.category_event
import community_app.composeapp.generated.resources.category_maintenance
import community_app.composeapp.generated.resources.category_other
import community_app.composeapp.generated.resources.status_active
import community_app.composeapp.generated.resources.status_cancelled
import community_app.composeapp.generated.resources.status_done
import community_app.composeapp.generated.resources.status_scheduled
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
    TicketCategory.INFRASTRUCTURE -> Res.string.welcome_back // TODO
    TicketCategory.CLEANING -> Res.string.welcome_back // TODO
    TicketCategory.SAFETY -> Res.string.welcome_back // TODO
    TicketCategory.NOISE -> Res.string.welcome_back // TODO
    TicketCategory.OTHER -> Res.string.welcome_back // TODO
  }
  return UiText.StringResourceId(res)
}

fun TicketStatus.toUiText(): UiText {
  val res = when(this) {
    TicketStatus.OPEN -> Res.string.welcome_back // TODO
    TicketStatus.IN_PROGRESS -> Res.string.welcome_back // TODO
    TicketStatus.RESOLVED -> Res.string.welcome_back // TODO
    TicketStatus.REJECTED -> Res.string.welcome_back // TODO
    TicketStatus.CANCELLED -> Res.string.welcome_back // TODO
  }
  return UiText.StringResourceId(res)
}