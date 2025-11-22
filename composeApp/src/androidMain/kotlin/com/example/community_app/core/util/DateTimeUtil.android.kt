package com.example.community_app.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
actual fun formatIsoDate(isoString: String): String {
  return formatIsoWithStyle(isoString, FormatStyle.MEDIUM, null)
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun formatIsoTime(isoString: String): String {
  return formatIsoWithStyle(isoString, null, FormatStyle.SHORT)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatIsoWithStyle(
  isoString: String,
  dateStyle: FormatStyle?,
  timeStyle: FormatStyle?
): String {
  return try {
    val instant = Instant.parse(isoString)

    val formatter = when {
      dateStyle != null && timeStyle != null -> DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)
      dateStyle != null -> DateTimeFormatter.ofLocalizedDate(dateStyle)
      timeStyle != null -> DateTimeFormatter.ofLocalizedTime(timeStyle)
      else -> return isoString
    }

    formatter
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault())
      .format(instant)
  } catch (e: Exception) {
    isoString
  }
}