package com.example.community_app.util

import com.example.community_app.model.Addresses
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere

/** Optionales BBOX-Filter auf bereits (left/inner) gejointem Locations-Table. */
fun Query.applyBbox(bbox: DoubleArray?): Query = apply {
  if (bbox != null && bbox.size == 4) {
    andWhere {
      (Addresses.longitude greaterEq bbox[0]) and
          (Addresses.longitude lessEq bbox[2]) and
          (Addresses.latitude  greaterEq bbox[1]) and
          (Addresses.latitude  lessEq bbox[3])
    }
  }
}
