package com.example.community_app.appointment.data.local.appointment

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
  @Upsert
  suspend fun upsertAppointments(appointments: List<AppointmentEntity>)

  @Query("SELECT * FROM appointments")
  fun getAppointments(): Flow<List<AppointmentEntity>>

  @Query("SELECT * FROM appointments WHERE id = :id")
  fun getAppointmentById(id: Int): Flow<AppointmentEntity?>

  @Query("DELETE FROM appointments WHERE id = :id")
  suspend fun deleteAppointment(id: Int)

  @Query("DELETE FROM appointments")
  suspend fun clearAll()

  @Transaction
  suspend fun replaceAll(appointments: List<AppointmentEntity>) {
    clearAll()
    upsertAppointments(appointments)
  }
}