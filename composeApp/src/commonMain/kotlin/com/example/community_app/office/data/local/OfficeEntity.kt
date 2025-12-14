package com.example.community_app.office.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offices")
data class OfficeEntity(
  @PrimaryKey(autoGenerate = false)
  val id: Int,
  val name: String,
  val description: String?,
  val services: String?,
  val openingHours: String?,
  val contactEmail: String?,
  val phone: String?,

  @Embedded(prefix = "addr_")
  val address: OfficeAddressEntity
)

data class OfficeAddressEntity(
  val street: String?,
  val houseNumber: String?,
  val zipCode: String?,
  val city: String?,
  val longitude: Double,
  val latitude: Double
)