package com.example.community_app.ticket.data.local.draft

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
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
  val lastModified: String
)

@Entity(
  tableName = "ticket_draft_images",
  foreignKeys = [
    ForeignKey(
      entity = TicketDraftEntity::class,
      parentColumns = ["id"],
      childColumns = ["draftId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("draftId")]
)
data class TicketDraftImageEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val draftId: Long,
  val localUri: String
)

data class TicketDraftWithImages(
  @Embedded val draft: TicketDraftEntity,
  @Relation(parentColumn = "id", entityColumn = "draftId")
  val images: List<TicketDraftImageEntity>
)