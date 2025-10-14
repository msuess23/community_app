package com.example.community_app.config

import com.example.community_app.util.Role
import com.example.community_app.model.*
import com.example.community_app.util.PasswordUtil
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object DatabaseSeeder {

  fun runIfEnabled(config: ApplicationConfig) {
    val enabled = config.propertyOrNull("ktor.seed.enabled")?.getString()?.toBooleanStrictOrNull() ?: false
    if (!enabled) return
    val password = config.propertyOrNull("ktor.seed.password")?.getString() ?: "password123!"
    val seedAppointments = config.propertyOrNull("ktor.seed.createAppointments")?.getString()?.toBooleanStrictOrNull() ?: false

    transaction {
      // 1) Office + Location (für OFFICER)
      val office = OfficeEntity.find { Offices.name eq "City Hall – Citizen Service" }.firstOrNull()
        ?: OfficeEntity.new {
          name = "City Hall – Citizen Service"
          description = "General administration / citizen services"
          services = "Registration, ID, Certificates"
          openingHours = "Mon–Fri 08:00–12:00"
          contactEmail = "service@city.local"
          phone = "000-000"
          location = LocationEntity.new {
            longitude = 8.404
            latitude = 49.009
            altitude = null
            accuracy = null
          }
        }

      // 2) Users (CITIZEN, OFFICER, ADMIN) – idempotent
      upsertUser(
        email = "citizen@dev.local",
        displayName = "Citizen Dev",
        role = Role.CITIZEN,
        officeId = null,
        plainPassword = password
      )
      upsertUser(
        email = "officer@dev.local",
        displayName = "Officer Dev",
        role = Role.OFFICER,
        officeId = office.id.value,
        plainPassword = password
      )
      upsertUser(
        email = "admin@dev.local",
        displayName = "Admin Dev",
        role = Role.ADMIN,
        officeId = null,
        plainPassword = password
      )

      // 3) (Optional) Beispiel-Slots für Office
      if (seedAppointments) {
        ensureSlot(officeId = office.id.value,
          startsAt = Instant.parse("2025-10-20T09:00:00Z"),
          endsAt   = Instant.parse("2025-10-20T09:15:00Z")
        )
        ensureSlot(officeId = office.id.value,
          startsAt = Instant.parse("2025-10-20T09:15:00Z"),
          endsAt   = Instant.parse("2025-10-20T09:30:00Z")
        )
      }
    }
  }

  private fun upsertUser(
    email: String,
    displayName: String,
    role: Role,
    officeId: Int?,
    plainPassword: String
  ) {
    val existing = UserEntity.find { Users.email eq email }.firstOrNull()
    if (existing != null) {
      // bring to desired state (no password rotation to keep idempotency predictable)
      existing.displayName = displayName
      existing.role = role
      existing.officeId = officeId
    } else {
      UserEntity.new {
        this.email = email
        this.displayName = displayName
        this.passwordHash = PasswordUtil.hash(plainPassword)
        this.role = role
        this.officeId = officeId
      }
    }
  }

  private fun ensureSlot(officeId: Int, startsAt: Instant, endsAt: Instant) {
    val office = OfficeEntity.findById(officeId) ?: return
    val exists = AppointmentEntity.find {
      (Appointments.office eq office.id) and (Appointments.startsAt eq startsAt)
    }.empty().not()

    if (!exists) {
      AppointmentEntity.new {
        this.office = office
        this.startsAt = startsAt
        this.endsAt = endsAt
        this.user = null
      }
    }
  }
}
