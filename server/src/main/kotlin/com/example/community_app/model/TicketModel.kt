package com.example.community_app.model

import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Tickets : IntIdTable(name = "TICKETS") {
  val title = varchar("TITLE", 255)
  val description = text("DESCRIPTION").nullable()
  val category = enumerationByName("CATEGORY", 32, TicketCategory::class)
  val creator = reference("CREATOR_USER_ID", Users, onDelete = ReferenceOption.CASCADE)
  val office = reference("OFFICE_ID", Offices, onDelete = ReferenceOption.SET_NULL).nullable()
  val address = reference("ADDRESS_ID", Addresses, onDelete = ReferenceOption.SET_NULL).nullable()
  val visibility = enumerationByName("VISIBILITY", 16, TicketVisibility::class).default(TicketVisibility.PUBLIC)
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)
}

class TicketEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<TicketEntity>(Tickets)
  var title by Tickets.title
  var description by Tickets.description
  var category by Tickets.category
  var creator by UserEntity referencedOn Tickets.creator
  var office by OfficeEntity optionalReferencedOn Tickets.office
  var address by AddressEntity optionalReferencedOn Tickets.address
  var visibility by Tickets.visibility
  var createdAt by Tickets.createdAt
}
