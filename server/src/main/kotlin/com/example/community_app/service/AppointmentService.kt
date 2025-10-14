package com.example.community_app.service

import com.example.community_app.dto.*
import com.example.community_app.errors.ConflictException
import com.example.community_app.errors.NotFoundException
import com.example.community_app.errors.ValidationException
import com.example.community_app.repository.AppointmentRecord
import com.example.community_app.repository.AppointmentRepository
import java.time.Instant
import java.time.format.DateTimeParseException

class AppointmentService(
  private val repo: AppointmentRepository
) {

  suspend fun listFreeSlots(officeId: Int, from: String?, to: String?): List<SlotDto> {
    val f = from?.let { parseInstant(it) }
    val t = to?.let { parseInstant(it) }
    return repo.listFreeSlots(officeId, f, t).map { it.toSlotDto() }
  }

  suspend fun createSlots(officeId: Int, batch: SlotBatchCreateDto): List<SlotDto> {
    if (batch.slots.isEmpty()) throw ValidationException("slots must not be empty")
    val pairs = batch.slots.map { parseSlot(it) }
    validateNoOverlaps(pairs)
    val recs = repo.createSlots(officeId, pairs)
    return recs.filter { it.userId == null }.map { it.toSlotDto() }
  }

  suspend fun deleteSlot(officeId: Int, slotId: Int): Boolean {
    return repo.deleteSlot(officeId, slotId)
  }

  suspend fun book(officeId: Int, dto: AppointmentCreateDto, userId: Int): AppointmentDto {
    val (s, e) = parseSlot(SlotCreateDto(dto.startsAt, dto.endsAt))
    try {
      val rec = repo.book(officeId, s, e, userId)
      return rec.toAppointmentDto()
    } catch (e: IllegalArgumentException) {
      throw NotFoundException(e.message ?: "Slot not found")
    } catch (e: IllegalStateException) {
      throw ConflictException("Slot already booked")
    }
  }

  suspend fun listForUser(userId: Int): List<AppointmentDto> =
    repo.listForUser(userId).map { it.toAppointmentDto() }

  suspend fun cancel(appointmentId: Int, userId: Int): Boolean =
    repo.cancel(appointmentId, userId)

  // --- helpers ---

  private fun parseInstant(s: String): Instant =
    try { Instant.parse(s) } catch (e: DateTimeParseException) {
      throw ValidationException("Invalid datetime: $s; expected ISO-8601 UTC")
    }

  private fun parseSlot(s: SlotCreateDto): Pair<Instant, Instant> {
    val start = parseInstant(s.startsAt)
    val end = parseInstant(s.endsAt)
    if (!end.isAfter(start)) throw ValidationException("endsAt must be after startsAt")
    return start to end
  }

  private fun validateNoOverlaps(slots: List<Pair<Instant, Instant>>) {
    val sorted = slots.sortedBy { it.first }
    for (i in 1 until sorted.size) {
      if (!sorted[i].first.isAfter(sorted[i - 1].second)) {
        throw ValidationException("Overlapping slots in batch")
      }
    }
  }

  private fun AppointmentRecord.toSlotDto() = SlotDto(
    id = id,
    startsAt = startsAt.toString(),
    endsAt = endsAt.toString()
  )

  private fun AppointmentRecord.toAppointmentDto() = AppointmentDto(
    id = id,
    officeId = officeId,
    startsAt = startsAt.toString(),
    endsAt = endsAt.toString(),
    userId = userId
  )
}
