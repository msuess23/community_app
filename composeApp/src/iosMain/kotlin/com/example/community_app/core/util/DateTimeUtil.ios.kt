package com.example.community_app.core.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

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

actual fun getStartOfDay(millis: Long): Long {
  val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
  val calendar = NSCalendar.currentCalendar
  val components = calendar.components(
    NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
    fromDate = date
  )
  return (calendar.dateFromComponents(components)?.timeIntervalSince1970?.times(1000))?.toLong() ?: millis
}

actual fun addDays(millis: Long, days: Int): Long {
  val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
  val calendar = NSCalendar.currentCalendar
  val newDate = calendar.dateByAddingUnit(
    unit = NSCalendarUnitDay,
    value = days.toLong(),
    toDate = date,
    options = 0u
  )
  return (newDate?.timeIntervalSince1970?.times(1000))?.toLong() ?: millis
}

actual fun parseIsoToMillis(isoString: String): Long {
  val formatter = NSISO8601DateFormatter()
  val date = formatter.dateFromString(isoString)
  return (date?.timeIntervalSince1970?.times(1000))?.toLong() ?: 0L
}

actual fun toIso8601(millis: Long): String {
  val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
  val formatter = NSISO8601DateFormatter()
  return formatter.stringFromDate(date)
}