package com.example.community_app.info.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "infos")
data class InfoEntity(
  @PrimaryKey(autoGenerate = false)
  val id: Int,
  val title: String,
  val description: String?,
  val category: String,
  val officeId: Int?,

  @Embedded(prefix = "addr_")
  val address: InfoAddressEntity?,

  val createdAt: String,
  val startsAt: String,
  val endsAt: String,
  val currentStatus: String?,
  val statusMessage: String?,
  val imageUrl: String?,
)

data class InfoAddressEntity(
  val street: String?,
  val houseNumber: String?,
  val zipCode: String?,
  val city: String?,
  val longitude: Double,
  val latitude: Double
)