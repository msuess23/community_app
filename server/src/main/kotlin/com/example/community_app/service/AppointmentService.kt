package com.example.community_app.service

import com.example.community_app.dto.*
import com.example.community_app.errors.ConflictException
import com.example.community_app.errors.NotFoundException
import com.example.community_app.errors.ValidationException
import com.example.community_app.repository.AppointmentRecord
import com.example.community_app.repository.AppointmentRepository
import com.example.community_app.util.parseInstantStrict
import java.time.Instant

class AppointmentService(
  private val repo: AppointmentRepository
) {

  /** Freie Slots einer Behörde listen (optional mit Zeitfenster). */
  suspend fun listFreeSlots(officeId: Int, from: String?, to: String?): List<SlotDto> {
    val f = from?.let { parseInstantStrict(it) }
    val t = to?.let { parseInstantStrict(it) }
    return repo.listFreeSlots(officeId, f, t).map { it.toSlotDto() }
  }

  /** Slots in Batch anlegen (Officer/Admin-Routing prüft Berechtigung). */
  suspend fun createSlots(officeId: Int, batch: SlotBatchCreateDto): List<SlotDto> {
    if (batch.slots.isEmpty()) throw ValidationException("slots must not be empty")
    val pairs = batch.slots.map { parseSlot(it) }
    validateNoOverlaps(pairs)
    val recs = repo.createSlots(officeId, pairs)
    return recs.map { it.toSlotDto() }
  }

  /** Einzelnen Slot löschen (nur freie Slots). */
  suspend fun deleteSlot(officeId: Int, slotId: Int): Boolean {
    val ok = repo.deleteSlot(officeId, slotId)
    if (!ok) throw NotFoundException("Slot not found")
    return true
  }

  /** Slot per ID buchen. */
  suspend fun bookById(officeId: Int, appointmentId: Int, userId: Int): AppointmentDto {
    val rec = repo.findById(appointmentId) ?: throw NotFoundException("Slot not found")
    if (rec.officeId != officeId) throw NotFoundException("Slot not found")
    if (rec.userId != null) throw ConflictException("Slot already booked")

    val booked = repo.bookSlot(appointmentId, userId) ?: throw ConflictException("Slot already booked")
    return booked.toAppointmentDto()
  }

  /** Alle Termine eines Users (Citizen) listen. */
  suspend fun listForUser(userId: Int): List<AppointmentDto> =
    repo.listForUser(userId).map { it.toAppointmentDto() }

  /** NEU: Detail eines eigenen Termins. */
  suspend fun getForUser(appointmentId: Int, userId: Int): AppointmentDto {
    val rec = repo.findById(appointmentId) ?: throw NotFoundException("Appointment not found")
    if (rec.userId != userId) throw NotFoundException("Appointment not found")
    return rec.toAppointmentDto()
  }

  /** Eigenen Termin stornieren. */
  suspend fun cancel(appointmentId: Int, userId: Int): Boolean =
    repo.cancel(appointmentId, userId)

  // --- helpers ---

  private fun parseSlot(s: SlotCreateDto): Pair<Instant, Instant> {
    val start = parseInstantStrict(s.startsAt)
    val end = parseInstantStrict(s.endsAt)
    if (!end.isAfter(start)) throw ValidationException("endsAt must be after startsAt")
    return start to end
  }

  private fun validateNoOverlaps(pairs: List<Pair<Instant, Instant>>) {
    val sorted = pairs.sortedBy { it.first }
    for (i in 1 until sorted.size) {
      val prev = sorted[i - 1]
      val cur = sorted[i]
      if (!cur.first.isAfter(prev.second)) {
        throw ValidationException("overlapping slots in input")
      }
    }
  }

  // --- mapping ---

  private fun AppointmentRecord.toSlotDto() = SlotDto(
    id = id,
    startsAt = startsAt.toString(),
    endsAt = endsAt.toString()
  )

  private fun AppointmentRecord.toAppointmentDto(): AppointmentDto {
    val uid = userId ?: throw IllegalStateException("Appointment record $id has no user, but represents an appointment!")

    return AppointmentDto(
      id = id,
      officeId = officeId,
      userId = uid,
      startsAt = startsAt.toString(),
      endsAt = endsAt.toString()
    )
  }
}
