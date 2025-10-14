package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Locations : IntIdTable(name = "LOCATIONS") {
  val longitude = double("LONGITUDE")
  val latitude = double("LATITUDE")
  val altitude = double("ALTITUDE").nullable()
  val accuracy = double("ACCURACY").nullable()
}

class LocationEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<LocationEntity>(Locations)
  var longitude by Locations.longitude
  var latitude by Locations.latitude
  var altitude by Locations.altitude
  var accuracy by Locations.accuracy
}
