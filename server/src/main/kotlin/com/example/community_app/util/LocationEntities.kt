package com.example.community_app.util

import com.example.community_app.dto.LocationDto
import com.example.community_app.model.LocationEntity

fun createLocation(dto: LocationDto): LocationEntity =
  LocationEntity.new {
    longitude = dto.longitude
    latitude = dto.latitude
    altitude = dto.altitude
    accuracy = dto.accuracy
  }

fun LocationEntity.updateFrom(dto: LocationDto) {
  longitude = dto.longitude
  latitude = dto.latitude
  altitude = dto.altitude
  accuracy = dto.accuracy
}