package com.example.community_app.geocoding.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "address_history")
data class AddressEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: Int,

  val formatted: String,
  val street: String?,
  val houseNumber: String?,
  val zipCode: String?,
  val city: String?,
  val country: String?,
  val latitude: Double,
  val longitude: Double,

  val type: String? = null,
  val lastUsedAt: Long
)