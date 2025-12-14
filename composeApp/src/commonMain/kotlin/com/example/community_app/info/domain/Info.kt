package com.example.community_app.info.domain

import com.example.community_app.core.domain.model.Address
import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus

data class Info(
  val id: Int,
  val title: String,
  val description: String?,
  val category: InfoCategory,
  val officeId: Int?,
  val address: Address?,
  val createdAt: String,
  val startsAt: String,
  val endsAt: String,
  val currentStatus: InfoStatus?,
  val statusMessage: String?,
  val imageUrl: String?,
  val isFavorite: Boolean = false
)