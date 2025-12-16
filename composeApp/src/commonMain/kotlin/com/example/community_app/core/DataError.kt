package com.example.community_app.core.domain

sealed interface DataError: Error {
  enum class Remote: DataError {
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER,
    SERIALIZATION,
    UNKNOWN
  }

  enum class Local: DataError {
    DISK_FULL,
    CALENDAR_EXPORT_FAILED,
    UNKNOWN
  }
}