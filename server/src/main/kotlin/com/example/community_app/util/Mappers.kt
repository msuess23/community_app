package com.example.community_app.util

import com.example.community_app.dto.LocationDto
import com.example.community_app.repository.LocationRecord

fun LocationRecord.toDto(): LocationDto =
  LocationDto(
    longitude = longitude,
    latitude  = latitude,
    altitude  = altitude,
    accuracy  = accuracy
  )
