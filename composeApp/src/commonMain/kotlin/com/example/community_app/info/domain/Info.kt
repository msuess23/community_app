package com.example.community_app.info.domain

data class Info(
  val id: Int,
  val title: String,
  val description: String?,
  val category: String,
  val officeId: Int,
  val createdAt: String,
  val startsAt: String?,
  val endsAt: String?,
  val imageUrl: String?,
)
