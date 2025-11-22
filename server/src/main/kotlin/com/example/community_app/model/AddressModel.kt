package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Addresses : IntIdTable(name = "ADDRESSES") {
  val street = varchar("STREET", 255).nullable()
  val houseNumber = varchar("HOUSE_NUMBER", 32).nullable()
  val zipCode = varchar("ZIP_CODE", 32).nullable()
  val city = varchar("CITY", 255).nullable()
  val longitude = double("LONGITUDE")
  val latitude = double("LATITUDE")
}

class AddressEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<AddressEntity>(Addresses)
  var street by Addresses.street
  var houseNumber by Addresses.houseNumber
  var zipCode by Addresses.zipCode
  var city by Addresses.city
  var longitude by Addresses.longitude
  var latitude by Addresses.latitude
}