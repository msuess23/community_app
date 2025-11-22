package com.example.community_app.core.util

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

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

  val formatter = NSDateFormatter()
  formatter.dateStyle = dateStyle
  formatter.timeStyle = timeStyle
  formatter.locale = NSLocale.currentLocale

  return formatter.stringFromDate(date)
}