package com.example.community_app.appointment.domain

data class Slot(
  val id: Int,
  val startIso: String,
  val endIso: String
)