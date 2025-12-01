package com.example.community_app.appointment.domain

data class Appointment(
  val id: Int,
  val officeId: Int,
  val userId: Int,
  val startsAt: String,
  val endsAt: String
)