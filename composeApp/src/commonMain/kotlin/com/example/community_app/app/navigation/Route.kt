package com.example.community_app.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable data object AuthGraph : Route
  @Serializable data object Login : Route
  @Serializable data object Register : Route
  @Serializable data object ForgotPassword : Route
  @Serializable data class ResetPassword(val email: String) : Route

  @Serializable data object InfoGraph: Route
  @Serializable data object InfoMaster: Route
  @Serializable data class InfoDetail(val id: Int): Route

  @Serializable data object TicketGraph : Route
  @Serializable data object TicketMaster : Route
  @Serializable data class TicketDetail(val id: Int): Route

  @Serializable data object OfficeGraph : Route
  @Serializable data object OfficeMaster : Route
  @Serializable data class OfficeDetail(val id: Int): Route

  @Serializable data object AppointmentGraph : Route
  @Serializable data object AppointmentMaster : Route
  @Serializable data class AppointmentDetail(val id: Int): Route

  @Serializable data object Settings : Route
}