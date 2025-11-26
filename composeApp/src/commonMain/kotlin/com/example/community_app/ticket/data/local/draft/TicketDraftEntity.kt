package com.example.community_app.ticket.data.local.draft

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.community_app.ticket.data.local.ticket.TicketAddressEntity

@Entity(tableName = "ticket_drafts")
data class TicketDraftEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long,
  val title: String,
  val description: String?,
  val category: String?,
  val officeId: Int?,

  @Embedded(prefix = "addr_")
  val address: TicketAddressEntity?,

  val visibility: String,
  val localImageUri: String?,
  val lastModified: String
)