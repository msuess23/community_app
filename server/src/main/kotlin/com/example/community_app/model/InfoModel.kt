package com.example.community_app.model

import com.example.community_app.util.InfoCategory
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Infos : IntIdTable(name = "INFOS") {
  val title = varchar("TITLE", 255)
  val description = text("DESCRIPTION").nullable()
  val category = enumerationByName("CATEGORY", 32, InfoCategory::class)
  val office = reference("OFFICE_ID", Offices, onDelete = ReferenceOption.SET_NULL).nullable()
  val address = reference("ADDRESS_ID", Addresses, onDelete = ReferenceOption.SET_NULL).nullable()
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)
  val startsAt = timestamp("STARTS_AT")
  val endsAt = timestamp("ENDS_AT")
}

class InfoEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<InfoEntity>(Infos)
  var title by Infos.title
  var description by Infos.description
  var category by Infos.category
  var office by OfficeEntity optionalReferencedOn Infos.office
  var address by AddressEntity optionalReferencedOn Infos.address
  var createdAt by Infos.createdAt
  var startsAt by Infos.startsAt
  var endsAt by Infos.endsAt
}
