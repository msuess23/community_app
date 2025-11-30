package com.example.community_app.core.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatIsoDate(isoString: String): String {
  return formatIso(
    isoString,
    dateStyle = NSDateFormatterMediumStyle,
    timeStyle = NSDateFormatterNoStyle
  )
}

actual fun formatIsoTime(isoString: String): String {
  return formatIso(
    isoString,
    dateStyle = NSDateFormatterNoStyle,
    timeStyle = NSDateFormatterShortStyle
  )
}

private fun formatIso(
  isoString: String,
  dateStyle: ULong,
  timeStyle: ULong
): String {
  val isoFormatter = NSISO8601DateFormatter()
  val date = isoFormatter.dateFromString(isoString) ?: return isoString
  return formatDateObject(date, dateStyle, timeStyle)
}

actual fun formatMillisDate(millis: Long): String {
  return formatMillis(millis, NSDateFormatterMediumStyle, NSDateFormatterNoStyle)
}

actual fun formatMillisTime(millis: Long): String {
  return formatMillis(millis, NSDateFormatterNoStyle, NSDateFormatterShortStyle)
}

private fun formatMillis(
  millis: Long,
  dateStyle: ULong,
  timeStyle: ULong
): String {
  val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
  return formatDateObject(date, dateStyle, timeStyle)
}

private fun formatDateObject(
  date: NSDate,
  dateStyle: ULong,
  timeStyle: ULong
): String {
  val currentTag = localeManager.getCurrentLocaleTag()
  val locale = NSLocale(currentTag)

  val formatter = NSDateFormatter()
  formatter.dateStyle = dateStyle
  formatter.timeStyle = timeStyle
  formatter.locale = locale

  return formatter.stringFromDate(date)
}