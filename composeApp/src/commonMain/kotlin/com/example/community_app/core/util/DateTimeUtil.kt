package com.example.community_app.core.util

expect fun formatIsoDate(isoString: String): String
expect fun formatIsoTime(isoString: String): String

expect fun formatMillisDate(millis: Long): String
expect fun formatMillisTime(millis: Long): String

expect fun getStartOfDay(millis: Long): Long
expect fun addDays(millis: Long, days: Int): Long
expect fun parseIsoToMillis(isoString: String): Long

expect fun toIso8601(millis: Long): String