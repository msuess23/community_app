package com.example.community_app.core.util

import com.example.community_app.core.domain.location.Location
import kotlin.math.*

object GeoUtil {
  const val EARTH_RADIUS = 6371.0

  fun calculateDistanceKm(loc1: Location, loc2: Location): Double {
    val dLat = toRadians(loc2.latitude - loc1.latitude)
    val dLon = toRadians(loc2.longitude - loc1.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(toRadians(loc1.latitude)) * cos(toRadians(loc2.latitude)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS * c
  }

  private fun toRadians(deg: Double): Double = deg * (PI / 180)
  private fun toDegrees(rad: Double): Double = rad * (180 / PI)

  fun calculateBBox(center: Location, radiusKm: Double): DoubleArray {
    val latDelta = toDegrees(radiusKm / EARTH_RADIUS)
    val minLat = center.latitude - latDelta
    val maxLat = center.latitude + latDelta

    val lonDelta = toDegrees(radiusKm / EARTH_RADIUS / cos(toRadians(center.latitude)))
    val minLon = center.longitude - lonDelta
    val maxLon = center.longitude + lonDelta

    return doubleArrayOf(minLon, minLat, maxLon, maxLat)
  }

  fun toBBoxString(bbox: DoubleArray): String {
    return "${bbox[0]},${bbox[1]},${bbox[2]},${bbox[3]}"
  }
}