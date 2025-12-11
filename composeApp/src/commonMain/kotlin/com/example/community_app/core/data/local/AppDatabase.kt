package com.example.community_app.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.community_app.appointment.data.local.AppointmentDao
import com.example.community_app.appointment.data.local.AppointmentEntity
import com.example.community_app.info.data.local.InfoDao
import com.example.community_app.info.data.local.InfoEntity
import com.example.community_app.office.data.local.OfficeDao
import com.example.community_app.office.data.local.OfficeEntity
import com.example.community_app.ticket.data.local.draft.TicketDraftDao
import com.example.community_app.ticket.data.local.draft.TicketDraftEntity
import com.example.community_app.ticket.data.local.draft.TicketDraftImageEntity
import com.example.community_app.ticket.data.local.ticket.TicketDao
import com.example.community_app.ticket.data.local.ticket.TicketEntity

@Database(
  entities = [
    InfoEntity::class,
    TicketEntity::class,
    TicketDraftEntity::class,
    TicketDraftImageEntity::class,
    OfficeEntity::class,
    AppointmentEntity::class
  ],
  version = 6
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun infoDao(): InfoDao
  abstract fun ticketDao(): TicketDao
  abstract fun ticketDraftDao(): TicketDraftDao
  abstract fun officeDao(): OfficeDao
  abstract fun appointmentDao(): AppointmentDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  override fun initialize(): AppDatabase
}