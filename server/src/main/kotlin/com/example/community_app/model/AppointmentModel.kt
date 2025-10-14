package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp

object Appointments : IntIdTable(name = "APPOINTMENTS") {
  val office = reference("OFFICE_ID", Offices, onDelete = ReferenceOption.CASCADE)
  val startsAt = timestamp("STARTS_AT")
  val endsAt = timestamp("ENDS_AT")
  val user = reference("USER_ID", Users, onDelete = ReferenceOption.SET_NULL).nullable()
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)

  init {
    index(true, office, startsAt) // unique (officeId, startsAt)
  }
}

class AppointmentEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<AppointmentEntity>(Appointments)
  var office by OfficeEntity referencedOn Appointments.office
  var startsAt by Appointments.startsAt
  var endsAt by Appointments.endsAt
  var user by UserEntity optionalReferencedOn Appointments.user
  var createdAt by Appointments.createdAt
}
