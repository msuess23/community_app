package com.example.community_app.repository

import com.example.community_app.model.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

data class AppointmentRecord(
  val id: Int,
  val officeId: Int,
  val startsAt: Instant,
  val endsAt: Instant,
  val userId: Int?
)

interface AppointmentRepository {
  suspend fun listFreeSlots(officeId: Int, from: Instant?, to: Instant?): List<AppointmentRecord>
  suspend fun createSlots(officeId: Int, slots: List<Pair<Instant, Instant>>): List<AppointmentRecord>
  suspend fun deleteSlot(officeId: Int, slotId: Int): Boolean
  suspend fun book(officeId: Int, startsAt: Instant, endsAt: Instant, userId: Int): AppointmentRecord
  suspend fun listForUser(userId: Int): List<AppointmentRecord>
  suspend fun cancel(appointmentId: Int, userId: Int): Boolean
}

object DefaultAppointmentRepository : AppointmentRepository {

  override suspend fun listFreeSlots(officeId: Int, from: Instant?, to: Instant?): List<AppointmentRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      val q = Appointments.selectAll().apply {
        andWhere { Appointments.office eq officeId }
        andWhere { Appointments.user.isNull() }
        if (from != null) andWhere { Appointments.startsAt greaterEq from }
        if (to != null) andWhere { Appointments.endsAt lessEq to }
      }

      q.orderBy(Appointments.startsAt to SortOrder.ASC)
        .map { it.toRecord() }
    }

  override suspend fun createSlots(officeId: Int, slots: List<Pair<Instant, Instant>>): List<AppointmentRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      val office = OfficeEntity.findById(officeId) ?: throw IllegalArgumentException("Office not found")
      val created = mutableListOf<AppointmentRecord>()
      for ((start, end) in slots) {
        val entity = AppointmentEntity.find {
          (Appointments.office eq office.id) and (Appointments.startsAt eq start)
        }.firstOrNull() ?: AppointmentEntity.new {
          this.office = office
          this.startsAt = start
          this.endsAt = end
          this.user = null
        }
        created.add(entity.toRecord())
      }
      created.sortedBy { it.startsAt }
    }

  override suspend fun deleteSlot(officeId: Int, slotId: Int): Boolean =
    newSuspendedTransaction(Dispatchers.IO) {
      val slot = AppointmentEntity.findById(slotId) ?: return@newSuspendedTransaction false
      if (slot.office.id.value != officeId) return@newSuspendedTransaction false
      if (slot.user != null) return@newSuspendedTransaction false
      slot.delete()
      true
    }

  override suspend fun book(officeId: Int, startsAt: Instant, endsAt: Instant, userId: Int): AppointmentRecord =
    newSuspendedTransaction(Dispatchers.IO) {
      val slot = AppointmentEntity.find {
        (Appointments.office eq officeId) and
            (Appointments.startsAt eq startsAt) and
            (Appointments.endsAt eq endsAt)
      }.firstOrNull() ?: throw IllegalArgumentException("Slot not found")

      if (slot.user != null) throw IllegalStateException("Slot already booked")

      slot.user = UserEntity.findById(userId) ?: throw IllegalArgumentException("User not found")
      slot.toRecord()
    }

  override suspend fun listForUser(userId: Int): List<AppointmentRecord> =
    newSuspendedTransaction(Dispatchers.IO) {
      AppointmentEntity.find { Appointments.user eq userId }
        .orderBy(Appointments.startsAt to SortOrder.ASC)
        .map { it.toRecord() }
    }

  override suspend fun cancel(appointmentId: Int, userId: Int): Boolean =
    newSuspendedTransaction(Dispatchers.IO) {
      val appt = AppointmentEntity.findById(appointmentId) ?: return@newSuspendedTransaction false
      if (appt.user?.id?.value != userId) return@newSuspendedTransaction false
      appt.user = null
      true
    }

  // --- mapping helpers ---
  private fun ResultRow.toRecord() = AppointmentRecord(
    id = this[Appointments.id].value,
    officeId = this[Appointments.office].value,
    startsAt = this[Appointments.startsAt],
    endsAt = this[Appointments.endsAt],
    userId = this[Appointments.user]?.value
  )

  private fun AppointmentEntity.toRecord() = AppointmentRecord(
    id = this.id.value,
    officeId = this.office.id.value,
    startsAt = this.startsAt,
    endsAt = this.endsAt,
    userId = this.user?.id?.value
  )
}
