package com.example.community_app.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object TicketVotes : IntIdTable(name = "TICKET_VOTES") {
  val ticket = reference("TICKET_ID", Tickets, onDelete = ReferenceOption.CASCADE)
  val user = reference("USER_ID", Users, onDelete = ReferenceOption.CASCADE)
  val createdAt = timestamp("CREATED_AT").defaultExpression(CurrentTimestamp)

  init { index(true, ticket, user) } // unique (ticket,user)
}

class TicketVoteEntity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<TicketVoteEntity>(TicketVotes)
  var ticket by TicketEntity referencedOn TicketVotes.ticket
  var user by UserEntity referencedOn TicketVotes.user
  var createdAt by TicketVotes.createdAt
}
