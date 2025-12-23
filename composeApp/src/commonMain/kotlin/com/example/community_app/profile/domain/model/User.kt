package com.example.community_app.profile.domain.model

data class User(
  val id: Int,
  val email: String,
  val displayName: String?
)