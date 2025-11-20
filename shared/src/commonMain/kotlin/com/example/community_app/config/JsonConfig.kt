package com.example.community_app.config

import kotlinx.serialization.json.Json

val AppJson = Json {
  ignoreUnknownKeys = true
  prettyPrint = true
  isLenient = true
  encodeDefaults = true
}