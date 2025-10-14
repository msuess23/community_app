package com.example.community_app.util

import com.example.community_app.model.Locations
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere

/** Optionales BBOX-Filter auf bereits (left/inner) gejointem Locations-Table. */
fun Query.applyBbox(bbox: DoubleArray?): Query = apply {
  if (bbox != null && bbox.size == 4) {
    andWhere {
      (Locations.longitude greaterEq bbox[0]) and
          (Locations.longitude lessEq bbox[2]) and
          (Locations.latitude  greaterEq bbox[1]) and
          (Locations.latitude  lessEq bbox[3])
    }
  }
}
