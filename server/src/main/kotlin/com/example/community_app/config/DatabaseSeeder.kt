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
    fun loc(latOffset: Double, lonOffset: Double) = HOME_LAT + latOffset to HOME_LON + lonOffset

    // --- 1) Offices + Addresses ---
    val (office1Id, office2Id) = transaction {
      val (o1Lat, o1Lon) = loc(0.0, 0.0)
      val addr1 = AddressEntity.new {
        street = "Alfons-Goppel-Platz"
        houseNumber = "1"
        zipCode = "95028"
        city = "Hof"
        longitude = o1Lon
        latitude = o1Lat
      }
      val office1 = OfficeEntity.new {
        name = "Studienbüro / Prüfungsamt"
        description = "Das Studienbüro ist Ihre erste Anlaufstelle, wenn Sie Fragen rund um Ihr Studium an der Hochschule haben. Hier reichen Sie auch Anträge ein, die an die Prüfungskommissionen weitergeleitet werden. Von hier aus werden Sie über Entscheidungen der Prüfungskommissionen und gegebenenfalls des Prüfungsausschusses benachrichtigt. Hier melden Sie sich, falls Sie einen Unfall am Campus oder auf dem Weg dorthin erlitten haben."
        services = "Fragen, Bescheinigungen, Prüfungen"
        openingHours = "Mo-Fr 9-12"
        contactEmail = "mail@hof-university.de"
        phone = "+49-9281-409"
        address = addr1
      }

      val (o2Lat, o2Lon) = loc(-2.2, 0.0)
      val addr2 = AddressEntity.new {
        street = "Marienplatz"
        houseNumber = "1"
        zipCode = "80331"
        city = "München"
        longitude = o2Lon
        latitude = o2Lat
      }
      val office2 = OfficeEntity.new {
        name = "Landeshauptstadt München"
        description = "Referat für Ordnung"
        services = "Großstadtverwaltung"
        openingHours = "Mo-Do 9-17"
        contactEmail = "info@muenchen.local"
        phone = "+49-89-123456"
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

    val (i1Lat, i1Lon) = loc(-0.3, 0.05)
    val info1a = infoRepo.create(InfoCreateData(
      title = "Festspiele Bayreuth (Mittel)",
      description = "Kulturveranstaltung (Mittelstrecke ~35km)",
      category = InfoCategory.EVENT,
      officeId = office1Id,
      address = AddressDto(street = "Hügel", longitude = i1Lon, latitude = i1Lat),
      startsAt = now.plus(5, ChronoUnit.DAYS),
      endsAt = now.plus(5, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS)
    ))

    val (i2Lat, i2Lon) = loc(0.004, 0.002)
    val info1b = infoRepo.create(InfoCreateData(
      title = "Baustelle Hauptstraße (Nah)",
      description = "Einschränkungen direkt im Ort (sehr nah, ~500m)",
      category = InfoCategory.CONSTRUCTION,
      officeId = office1Id,
      address = AddressDto(street = "Hauptstraße", longitude = i2Lon, latitude = i2Lat),
      startsAt = now,
      endsAt = now.plus(20, ChronoUnit.DAYS)
    ))

    val (i3Lat, i3Lon) = loc(-2.2, 0.0)
    val info2a = infoRepo.create(InfoCreateData(
      title = "Bürgerdialog München (Fern)",
      description = "Weit entfernt (>200km), sollte nicht geladen werden",
      category = InfoCategory.ANNOUNCEMENT,
      officeId = office2Id,
      address = AddressDto(street = "Odeonsplatz", longitude = i3Lon, latitude = i3Lat),
      startsAt = now.plus(2, ChronoUnit.DAYS),
      endsAt = now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
    ))

    val (i4Lat, i4Lon) = loc(0.01, 0.01)
    val info2b = infoRepo.create(InfoCreateData(
      title = "Parksanierung",
      description = "Neugestaltung des Stadtparks",
      category = InfoCategory.MAINTENANCE,
      officeId = office2Id,
      address = AddressDto(street = "Englischer Garten", longitude = i4Lon, latitude = i4Lat),
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

    val (t1Lat, t1Lon) = loc(-0.002, -0.001)
    val pub1 = ticketRepo.create(TicketCreateData(
      title = "Defektes Klettergerüst (Nah)",
      description = "Spielplatz um die Ecke (~200m)",
      category = TicketCategory.INFRASTRUCTURE,
      officeId = office1Id,
      creatorUserId = citizen1Id,
      address = AddressDto(street = "Spielplatzweg", longitude = t1Lon, latitude = t1Lat),
      visibility = TicketVisibility.PUBLIC
    ))

    val (t2Lat, t2Lon) = loc(-0.31, 0.04)
    val priv1 = ticketRepo.create(TicketCreateData(
      title = "Laterne Bayreuth (Mittel)",
      description = "Mein privates Ticket (~35km)",
      category = TicketCategory.INFRASTRUCTURE,
      officeId = office1Id,
      creatorUserId = citizen1Id,
      address = AddressDto(street = "Wagner-Str", longitude = t2Lon, latitude = t2Lat),
      visibility = TicketVisibility.PRIVATE
    ))

    val (t3Lat, t3Lon) = loc(-2.21, 0.01)
    val pub2 = ticketRepo.create(TicketCreateData(
      title = "Müll Isartor (Fern)",
      description = "Weit entferntes Ticket (>200km)",
      category = TicketCategory.CLEANING,
      officeId = office2Id,
      creatorUserId = citizen2Id,
      address = AddressDto(street = "Isartor", longitude = t3Lon, latitude = t3Lat),
      visibility = TicketVisibility.PUBLIC
    ))


    // Ticket-Status
    statusService.addTicketStatus(priv1.id, TicketStatus.OPEN, "Erfasst", citizen1Id)
    statusService.addTicketStatus(pub2.id, TicketStatus.OPEN, "Erfasst", citizen2Id)
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
      return destinationFile.length() to sourceFilename
    } else {
      createPlaceholderFile(destinationFile)
      return destinationFile.length() to sourceFilename
    }
  }

  private suspend fun seedMedia(targetType: MediaTargetType, targetId: Int, files: List<MediaSeed>) {
    val mediaRepo = DefaultMediaRepository

    files.forEach { seed ->
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