package com.example.community_app.appointment.domain.model

data class Slot(
  val id: Int,
  val startIso: String,
  val endIso: String
)