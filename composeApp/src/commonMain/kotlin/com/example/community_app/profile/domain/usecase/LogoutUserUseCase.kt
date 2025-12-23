package com.example.community_app.profile.domain.usecase

import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository
import com.example.community_app.appointment.domain.repository.AppointmentRepository
import com.example.community_app.auth.domain.repository.AuthRepository
import com.example.community_app.geocoding.domain.repository.AddressRepository
import com.example.community_app.info.domain.repository.InfoRepository
import com.example.community_app.office.domain.repository.OfficeRepository
import com.example.community_app.profile.domain.repository.UserRepository
import com.example.community_app.ticket.domain.repository.TicketRepository

class LogoutUserUseCase(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
  private val addressRepository: AddressRepository,
  private val ticketRepository: TicketRepository,
  private val appointmentNoteRepository: AppointmentNoteRepository,
  private val infoRepository: InfoRepository,
  private val officeRepository: OfficeRepository,
  private val appointmentRepository: AppointmentRepository
) {
  suspend operator fun invoke(clearData: Boolean) {
    if (clearData) {
      addressRepository.clearAllForUser()
      ticketRepository.clearUserData()
      appointmentNoteRepository.clearUserData()
    }

    userRepository.clearUser()
    authRepository.logout()

//    try {
//      infoRepository.refreshInfos(force = true)
//      ticketRepository.refreshTickets(force = true)
//      officeRepository.refreshOffices(force = true)
//      appointmentRepository.refreshAppointments(force = true)
//    } catch (e: Exception) {
//      e.printStackTrace()
//    }
  }
}