package com.example.community_app.core.presentation.helpers

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.presentation.helpers.UiText
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.error_disk_full
import community_app.composeapp.generated.resources.error_no_internet
import community_app.composeapp.generated.resources.error_request_timeout
import community_app.composeapp.generated.resources.error_serialization
import community_app.composeapp.generated.resources.error_too_many_requests
import community_app.composeapp.generated.resources.error_unknown

fun DataError.toUiText(): UiText {
  val stringResource = when(this) {
    DataError.Local.DISK_FULL -> Res.string.error_disk_full
    DataError.Local.UNKNOWN -> Res.string.error_unknown
    DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_request_timeout
    DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
    DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
    DataError.Remote.SERVER -> Res.string.error_unknown
    DataError.Remote.SERIALIZATION -> Res.string.error_serialization
    DataError.Remote.UNKNOWN -> Res.string.error_unknown
  }

  return UiText.StringResourceId(stringResource)
}