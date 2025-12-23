package com.example.community_app.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
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
    formatInstant(instant, dateStyle, timeStyle)
  } catch (e: Exception) {
    isoString
  }
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun formatMillisDate(millis: Long): String {
  return formatMillisWithStyle(millis, FormatStyle.MEDIUM, null)
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun formatMillisTime(millis: Long): String {
  return formatMillisWithStyle(millis, null, FormatStyle.SHORT)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatMillisWithStyle(
  millis: Long,
  dateStyle: FormatStyle?,
  timeStyle: FormatStyle?
): String {
  return try {
    val instant = Instant.ofEpochMilli(millis)
    formatInstant(instant, dateStyle, timeStyle)
  } catch (e: Exception) {
    ""
  }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatInstant(
  instant: Instant,
  dateStyle: FormatStyle?,
  timeStyle: FormatStyle?
): String {
  val formatter = when {
    dateStyle != null && timeStyle != null -> DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)
    dateStyle != null -> DateTimeFormatter.ofLocalizedDate(dateStyle)
    timeStyle != null -> DateTimeFormatter.ofLocalizedTime(timeStyle)
    else -> return ""
  }

  val currentTag = localeManager.getCurrentLocaleTag()
  val locale = Locale.forLanguageTag(currentTag)

  val formattedString = formatter
    .withLocale(locale)
    .withZone(ZoneId.systemDefault())
    .format(instant)

  if (currentTag.startsWith("de") && dateStyle == null) {
    return "$formattedString Uhr"
  }

  return formattedString
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun getStartOfDay(millis: Long): Long {
  val zone = ZoneId.systemDefault()
  return Instant.ofEpochMilli(millis)
    .atZone(zone)
    .toLocalDate()
    .atStartOfDay(zone)
    .toInstant()
    .toEpochMilli()
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun addDays(millis: Long, days: Int): Long {
  return Instant.ofEpochMilli(millis)
    .plus(days.toLong(), ChronoUnit.DAYS)
    .toEpochMilli()
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun parseIsoToMillis(isoString: String): Long {
  return try {
    Instant.parse(isoString).toEpochMilli()
  } catch (e: Exception) {
    0L
  }
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun toIso8601(millis: Long): String {
  return Instant.ofEpochMilli(millis).toString()
}
