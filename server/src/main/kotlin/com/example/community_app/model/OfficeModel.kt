package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Offices : IntIdTable(name = "OFFICES") {
  val name = varchar("NAME", 255)
  val description = text("DESCRIPTION").nullable()
  val services = text("SERVICES").nullable()
  val openingHours = text("OPENING_HOURS").nullable()
  val contactEmail = varchar("CONTACT_EMAIL", 255).nullable()
  val phone = varchar("PHONE", 64).nullable()
  val location = reference("LOCATION_ID", Locations, onDelete = ReferenceOption.CASCADE)
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)
}

class OfficeEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<OfficeEntity>(Offices)
  var name by Offices.name
  var description by Offices.description
  var services by Offices.services
  var openingHours by Offices.openingHours
  var contactEmail by Offices.contactEmail
  var phone by Offices.phone
  var location by LocationEntity referencedOn Offices.location
  var createdAt by Offices.createdAt
}
