package com.example.community_app.ticket.data.local.ticket

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
  @PrimaryKey(autoGenerate = false)
  val id: Int,
  val title: String,
  val description: String?,
  val category: String,
  val officeId: Int?,
  val creatorUserId: Int,

  @Embedded(prefix = "addr_")
  val address: TicketAddressEntity?,

  val visibility: String,
  val createdAt: String,
  val currentStatus: String?,
  val statusMessage: String?,
  val votesCount: Int,
  val userVoted: Boolean?,
  val imageUrl: String?,
)

data class TicketAddressEntity(
  val street: String?,
  val houseNumber: String?,
  val zipCode: String?,
  val city: String?,
  val longitude: Double,
  val latitude: Double
)