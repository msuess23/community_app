package com.example.community_app.core.domain.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

class AndroidCalendarManager(
  private val context: Context
) : CalendarManager {
  override suspend fun addEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Long,
    endMillis: Long
  ): String? = withContext(Dispatchers.IO) {
    val calendarId = getDefaultCalendarId() ?: return@withContext null

    val values = ContentValues().apply {
      put(CalendarContract.Events.DTSTART, startMillis)
      put(CalendarContract.Events.DTEND, endMillis)
      put(CalendarContract.Events.TITLE, title)
      put(CalendarContract.Events.DESCRIPTION, description)
      put(CalendarContract.Events.EVENT_LOCATION, location)
      put(CalendarContract.Events.CALENDAR_ID, calendarId)
      put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    try {
      val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
      uri?.lastPathSegment
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  override suspend fun removeEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId.toLong())
      val rows = context.contentResolver.delete(deleteUri, null, null)
      rows > 0
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  private fun getDefaultCalendarId(): Long? {
    val projection = arrayOf(
      CalendarContract.Calendars._ID,
      CalendarContract.Calendars.IS_PRIMARY
    )

    val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
    val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())

    try {
      context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          do {
            val idIndex = 0
            val primaryIndex = 1

            val id = cursor.getLong(idIndex)
            val isPrimary = cursor.getString(primaryIndex) == "1"

            if (isPrimary) return id
          } while (cursor.moveToNext())

          if (cursor.moveToFirst()) {
            return cursor.getLong(0)
          }
        }
      }
    } catch (e: SecurityException) {
      e.printStackTrace()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }
}