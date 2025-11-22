package com.example.community_app.core.presentation.helpers

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
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