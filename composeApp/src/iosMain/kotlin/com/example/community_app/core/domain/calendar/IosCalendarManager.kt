package com.example.community_app.core.domain.calendar

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970

class IosCalendarManager : CalendarManager {
  private val eventStore = EKEventStore()

  @OptIn(ExperimentalForeignApi::class)
  override suspend fun addEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Long,
    endMillis: Long
  ): String? = withContext(Dispatchers.IO) {
    val event = EKEvent.eventWithEventStore(eventStore)
    event.title = title
    event.notes = description
    event.location = location
    event.startDate = NSDate.dateWithTimeIntervalSince1970(startMillis / 1000.0)
    event.endDate = NSDate.dateWithTimeIntervalSince1970(endMillis / 1000.0)
    event.calendar = eventStore.defaultCalendarForNewEvents ?: return@withContext null

    try {
      eventStore.saveEvent(event, span = platform.EventKit.EKSpan.EKSpanThisEvent, error = null)
      event.eventIdentifier
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  @OptIn(ExperimentalForeignApi::class)
  override suspend fun removeEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
    val event = eventStore.eventWithIdentifier(eventId) ?: return@withContext false
    try {
      eventStore.removeEvent(event, span = platform.EventKit.EKSpan.EKSpanThisEvent, error = null)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}