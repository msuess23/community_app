package com.example.community_app.core.domain.calendar

interface CalendarManager {
  suspend fun addEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Long,
    endMillis: Long
  ): String?

  suspend fun removeEvent(eventId: String): Boolean
}