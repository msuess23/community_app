package com.example.community_app.config

import com.example.community_app.dto.AddressDto // LocationDto ersetzt durch AddressDto
import com.example.community_app.model.*
import com.example.community_app.repository.*
import com.example.community_app.service.StatusService
import com.example.community_app.util.*
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

object DatabaseSeeder {
  fun runIfEnabled(config: ApplicationConfig) {
    val enabled = config.config("ktor.seed").propertyOrNull("enabled")?.getString().toBoolean()

    if (!enabled) return

    transaction {
      // Locations muss durch Addresses ersetzt werden
      SchemaUtils.drop(Users, Settings, Addresses, Offices, Appointments, Infos, StatusEntries, Tickets, TicketVotes, Media)
      SchemaUtils.create(Users, Settings, Addresses, Offices, Appointments, Infos, StatusEntries, Tickets, TicketVotes, Media)
    }

    println("DevSeeder: start seeding…")
    runBlocking {
      try {
        seedAll()
      } catch (t: Throwable) {
        println("DevSeeder: ERROR during seed → ${t::class.simpleName}: ${t.message}")
        t.printStackTrace()
      }
    }
  }

  // --------------------------------------------------------------------------

  private suspend fun seedAll() {
    // --- 1) Offices + Addresses ---
    val (office1Id, office2Id) = transaction {
      // Adresse mit Adressfeldern erstellen
      val addr1 = AddressEntity.new {
        street = "Marienplatz"
        houseNumber = "1"
        zipCode = "80331"
        city = "München"
        longitude = 11.576124
        latitude = 48.137154
      }
      val office1 = OfficeEntity.new {
        name = "Einwohnermeldeamt Altstadt"
        description = "Meldebescheinigungen, Ausweise"
        services = "Meldewesen, Ausweise"
        openingHours = "Mo-Fr 8-16"
        contactEmail = "altstadt@city.example"
        phone = "+49-89-123456"
        address = addr1
      }

      // Adresse mit Adressfeldern erstellen
      val addr2 = AddressEntity.new {
        street = "Sendlinger Straße"
        houseNumber = "1"
        zipCode = "80331"
        city = "München"
        longitude = 11.581981
        latitude = 48.135125
      }
      val office2 = OfficeEntity.new {
        name = "Bürgerbüro Zentrum"
        description = "Bürgeranliegen, Fundbüro"
        services = "Anliegen, Fundbüro"
        openingHours = "Mo-Do 9-17"
        contactEmail = "zentrum@city.example"
        phone = "+49-89-654321"
        address = addr2
      }
      Pair(office1.id.value, office2.id.value)
    }

    // --- 2) Users (Admin, Officers, Citizens) ---
    val argonPassword = PasswordUtil.hash("password!123")

    val (adminId, officer1Id, officer2Id, citizen1Id, citizen2Id) = transaction {
      val admin = UserEntity.new {
        email = "admin@demo.local"
        displayName = "Admin"
        passwordHash = argonPassword
        role = Role.ADMIN
        officeId = null
      }
      val officer1 = UserEntity.new {
        email = "officer1@demo.local"
        displayName = "Officer Altstadt"
        passwordHash = argonPassword
        role = Role.OFFICER
        officeId = office1Id
      }
      val officer2 = UserEntity.new {
        email = "officer2@demo.local"
        displayName = "Officer Zentrum"
        passwordHash = argonPassword
        role = Role.OFFICER
        officeId = office2Id
      }
      val citizen1 = UserEntity.new {
        email = "citizen1@demo.local"
        displayName = "Max Mustermann"
        passwordHash = argonPassword
        role = Role.CITIZEN
        officeId = null
      }
      val citizen2 = UserEntity.new {
        email = "citizen2@demo.local"
        displayName = "Erika Musterfrau"
        passwordHash = argonPassword
        role = Role.CITIZEN
        officeId = null
      }
      Quintet(admin.id.value, officer1.id.value, officer2.id.value, citizen1.id.value, citizen2.id.value)
    }

    // --- 3) Settings ---
    transaction {
      val user = UserEntity.findById(citizen1Id)!!
      SettingsEntity.new {
        this.user = user
        language = "de"
        theme = "dark"
        notificationsEnabled = true
        syncEnabled = true
      }
    }

    // --- 4) Appointment-Slots + Buchungen ---
    val apptRepo = DefaultAppointmentRepository
    val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
    val slotsOffice1 = listOf(
      now.plus(1, ChronoUnit.DAYS) to now.plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
      now.plus(1, ChronoUnit.DAYS).plus(40, ChronoUnit.MINUTES) to now.plus(1, ChronoUnit.DAYS).plus(70, ChronoUnit.MINUTES),
      now.plus(2, ChronoUnit.DAYS) to now.plus(2, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
    )
    val slotsOffice2 = listOf(
      now.plus(1, ChronoUnit.DAYS) to now.plus(1, ChronoUnit.DAYS).plus(20, ChronoUnit.MINUTES),
      now.plus(3, ChronoUnit.DAYS) to now.plus(3, ChronoUnit.DAYS).plus(20, ChronoUnit.MINUTES),
    )

    val created1 = apptRepo.createSlots(office1Id, slotsOffice1)
    val created2 = apptRepo.createSlots(office2Id, slotsOffice2)

    created1.firstOrNull()?.let { apptRepo.bookSlot(it.id, citizen1Id) }
    created2.firstOrNull()?.let { apptRepo.bookSlot(it.id, citizen2Id) }

    // --- 5) Infos + Status je Office ---
    val infoRepo = DefaultInfoRepository
    val statusService = StatusService.default()

    val info1a = infoRepo.create(InfoCreateData(
      title = "Altstadtfest",
      description = "Live-Musik und Marktstände",
      category = InfoCategory.EVENT,
      officeId = office1Id,
      // AddressDto verwenden
      address = AddressDto(street = "Marienplatz", houseNumber = "1", zipCode = "80331", city = "München", longitude = 11.575, latitude = 48.1378),
      startsAt = now.plus(5, ChronoUnit.DAYS),
      endsAt = now.plus(5, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS)
    ))
    val info1b = infoRepo.create(InfoCreateData(
      title = "Baustelle Sendlinger Tor",
      description = "Einschränkungen im Verkehr",
      category = InfoCategory.CONSTRUCTION,
      officeId = office1Id,
      // AddressDto verwenden
      address = AddressDto(street = "Sendlinger Tor Platz", longitude = 11.565, latitude = 48.132),
      startsAt = now,
      endsAt = now.plus(20, ChronoUnit.DAYS)
    ))

    val info2a = infoRepo.create(InfoCreateData(
      title = "Bürgerdialog im Zentrum",
      description = "Sprechstunde des Bürgermeisters",
      category = InfoCategory.ANNOUNCEMENT,
      officeId = office2Id,
      // AddressDto verwenden
      address = AddressDto(street = "Odeonsplatz", longitude = 11.5823, latitude = 48.1369),
      startsAt = now.plus(2, ChronoUnit.DAYS),
      endsAt = now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
    ))
    val info2b = infoRepo.create(InfoCreateData(
      title = "Parksanierung",
      description = "Neugestaltung des Stadtparks",
      category = InfoCategory.MAINTENANCE,
      officeId = office2Id,
      // AddressDto verwenden
      address = AddressDto(street = "Englischer Garten", longitude = 11.59, latitude = 48.138),
      startsAt = now.plus(1, ChronoUnit.DAYS),
      endsAt = now.plus(14, ChronoUnit.DAYS)
    ))

    // Statusverläufe
    statusService.addInfoStatus(info1a.id, InfoStatus.SCHEDULED, "Termin steht", adminId)
    statusService.addInfoStatus(info1b.id, InfoStatus.ACTIVE, "Arbeiten begonnen", officer1Id)
    statusService.addInfoStatus(info2a.id, InfoStatus.SCHEDULED, "Vorbereitung läuft", officer2Id)
    statusService.addInfoStatus(info2b.id, InfoStatus.SCHEDULED, "Planung gestartet", officer2Id)

    try {
      seedMedia(
        targetType = MediaTargetType.INFO,
        targetId = info1b.id,
        files = listOf(
          MediaSeed(sourceFilename = "istockphoto-construction-1.jpg", isCover = true, mimeType = "image/jpeg"),
          MediaSeed(sourceFilename = "istockphoto-construction-2.jpg", isCover = false, mimeType = "image/jpeg"),
          MediaSeed(sourceFilename = "istockphoto-construction-3.jpg", isCover = false, mimeType = "image/jpeg")
        )
      )
    } catch (e: Throwable) {
      println("DevSeeder: could not seed media for Info: ${e.message}")
    }

    // --- 6) Tickets (je Citizen ein privates) + 1 öffentliches + Vote ---
    val ticketRepo = DefaultTicketRepository

    val priv1 = ticketRepo.create(TicketCreateData(
      title = "Defekte Laterne in der Altstadt",
      description = "Laterne flackert permanent",
      category = TicketCategory.INFRASTRUCTURE,
      officeId = office1Id,
      creatorUserId = citizen1Id,
      // AddressDto verwenden
      address = AddressDto(street = "Kaufinger Straße", longitude = 11.574, latitude = 48.137),
      visibility = TicketVisibility.PRIVATE
    ))
    val priv2 = ticketRepo.create(TicketCreateData(
      title = "Wilder Müll im Zentrum",
      description = "Mehrere Säcke am Wegesrand",
      category = TicketCategory.CLEANING, // verwende dein aktuelles Enum
      officeId = office2Id,
      creatorUserId = citizen2Id,
      // AddressDto verwenden
      address = AddressDto(street = "Isartorplatz", longitude = 11.583, latitude = 48.136),
      visibility = TicketVisibility.PRIVATE
    ))
    val pub1 = ticketRepo.create(TicketCreateData(
      title = "Spielplatz: kaputtes Klettergerüst",
      description = "Bitte prüfen und reparieren",
      category = TicketCategory.INFRASTRUCTURE,
      officeId = office1Id,
      creatorUserId = citizen1Id,
      // AddressDto verwenden
      address = AddressDto(street = "Hofgarten", longitude = 11.5765, latitude = 48.1375),
      visibility = TicketVisibility.PUBLIC
    ))

    // Ticket-Status
    statusService.addTicketStatus(priv1.id, TicketStatus.OPEN, "Erfasst", citizen1Id)
    statusService.addTicketStatus(priv2.id, TicketStatus.OPEN, "Erfasst", citizen2Id)
    statusService.addTicketStatus(pub1.id, TicketStatus.OPEN, "Erfasst", citizen1Id)
    statusService.addTicketStatus(pub1.id, TicketStatus.IN_PROGRESS, "Begutachtung läuft", officer1Id)

    // Vote
    ticketRepo.addVote(pub1.id, citizen2Id)

    try {
      seedMedia(
        targetType = MediaTargetType.TICKET,
        targetId = pub1.id,
        files = listOf(
          MediaSeed(sourceFilename = "istockphoto-playground-1.jpg", isCover = true, mimeType = "image/jpeg"),
          MediaSeed(sourceFilename = "istockphoto-playground-2.jpg", isCover = false, mimeType = "image/jpeg")
        )
      )
    } catch (e: Throwable) {
      println("DevSeeder: could not seed demo media for pub1: ${e.message}")
    }
  }

  // --- Media Helpers ---
  private data class MediaSeed(
    val sourceFilename: String,
    val isCover: Boolean,
    val mimeType: String
  )

  /** * Simuliert das Hochladen: Kopiert eine Datei vom Quellort in den Zielort
   * und gibt Metadaten zurück.
   * Du musst diese Funktion anpassen, um deine Bilder zu kopieren.
   */
  private fun readSourceFileAndGetDetails(type: MediaTargetType, targetId: Int, sourceFilename: String): Pair<Long, String> {
    val destinationDir = MediaConfig.targetDir(type.name, targetId)
    val destinationFile = File(destinationDir, sourceFilename)

    val sourceFile = File("src/main/resources/seed_images", sourceFilename)
    if (sourceFile.exists()) {
      sourceFile.copyTo(destinationFile, overwrite = true)
      // KORREKTUR: Gibt den eindeutigen Dateinamen zurück, nicht den festen String "OK".
      return destinationFile.length() to sourceFilename
    } else {
      // FALLBACK: Tiny PNG als Platzhalter erstellen
      createPlaceholderFile(destinationFile)
      // KORREKTUR: Gibt den eindeutigen Dateinamen zurück, nicht den festen String "PLACEHOLDER".
      return destinationFile.length() to sourceFilename
    }
  }

  private suspend fun seedMedia(targetType: MediaTargetType, targetId: Int, files: List<MediaSeed>) {
    val mediaRepo = DefaultMediaRepository

    files.forEach { seed ->
      // Wir übergeben den sourceFilename, der auch der serverFilename wird.
      val (sizeBytes, finalFilename) = readSourceFileAndGetDetails(
        targetType, targetId, seed.sourceFilename
      )

      mediaRepo.create(
        MediaCreateData(
          targetType = targetType,
          targetId = targetId,
          serverFilename = finalFilename,
          originalFilename = seed.sourceFilename,
          mimeType = seed.mimeType,
          sizeBytes = sizeBytes,
          isCover = seed.isCover
        )
      )
    }
  }

  /** Erstellt einen winzigen 1x1 PNG Platzhalter an einem File-Pfad. */
  private fun createPlaceholderFile(file: File) {
    val bytes = Base64.getDecoder().decode(
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII="
    )
    if (!file.parentFile.exists()) file.parentFile.mkdirs()
    file.outputStream().use { it.write(bytes) }
  }

  private data class Quintet<A,B,C,D,E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}