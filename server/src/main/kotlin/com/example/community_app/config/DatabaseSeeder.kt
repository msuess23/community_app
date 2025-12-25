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

      val (o2Lat, o2Lon) = loc(0.170, 0.195)
      val addr2 = AddressEntity.new {
        street = "Unterer Graben"
        houseNumber = "1"
        zipCode = "08523"
        city = "Plauen"
        longitude = o2Lon
        latitude = o2Lat
      }
      val office2 = OfficeEntity.new {
        name = "Stadtverwaltung Plauen"
        description = "Bürgerbüro der Stadt Plauen. Zuständig für Meldeangelegenheiten, Pässe und gewerbliche Anliegen im Vogtlandkreis."
        services = "Einwohnermeldeamt, Gewerbeamt, Fundbüro"
        openingHours = "Mo 09-15, Do 09-18 Uhr"
        contactEmail = "buergerbuero@plauen.de"
        phone = "+49-3741-2910"
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

    val (i1aLat, i1aLon) = loc(0.015, -0.015)
    val info1a = infoRepo.create(InfoCreateData(
      title = "Konzert in der Freiheitshalle",
      description = "Großes Benefizkonzert der Hofer Symphoniker. Einlass ab 18 Uhr.",
      category = InfoCategory.EVENT,
      officeId = office1Id,
      address = AddressDto(street = "Kulmbacher Str.", houseNumber = "4", zipCode = "95030", city = "Hof", longitude = i1aLon, latitude = i1aLat),
      startsAt = now.plus(7, ChronoUnit.DAYS).plus(19, ChronoUnit.HOURS),
      endsAt = now.plus(7, ChronoUnit.DAYS).plus(23, ChronoUnit.HOURS)
    ))

    val (i1bLat, i1bLon) = loc(-0.005, 0.005)
    val info1b = infoRepo.create(InfoCreateData(
      title = "Kanalarbeiten Ernst-Reuter-Str.",
      description = "Vollsperrung wegen Erneuerung der Wasserleitungen. Umleitung über Wunsiedler Straße.",
      category = InfoCategory.CONSTRUCTION,
      officeId = office1Id,
      address = AddressDto(street = "Ernst-Reuter-Str.", houseNumber = "15", zipCode = "95028", city = "Hof", longitude = i1bLon, latitude = i1bLat),
      startsAt = now,
      endsAt = now.plus(14, ChronoUnit.DAYS)
    ))

    val (i2aLat, i2aLon) = loc(0.172, 0.196)
    val info2a = infoRepo.create(InfoCreateData(
      title = "Plauener Spitzenfest",
      description = "Traditionelles Stadtfest auf dem Altmarkt mit Live-Musik und Handwerksmarkt.",
      category = InfoCategory.EVENT,
      officeId = office2Id,
      address = AddressDto(street = "Altmarkt", houseNumber = "1", zipCode = "08523", city = "Plauen", longitude = i2aLon, latitude = i2aLat),
      startsAt = now.plus(20, ChronoUnit.DAYS),
      endsAt = now.plus(22, ChronoUnit.DAYS)
    ))

    val (i2bLat, i2bLon) = loc(0.168, 0.192)
    val info2b = infoRepo.create(InfoCreateData(
      title = "Wartung Stadtbad",
      description = "Das Stadtbad bleibt wegen jährlicher Revisionsarbeiten geschlossen.",
      category = InfoCategory.MAINTENANCE,
      officeId = office2Id,
      address = AddressDto(street = "Hofer Str.", houseNumber = "2", zipCode = "08527", city = "Plauen", longitude = i2bLon, latitude = i2bLat),
      startsAt = now.plus(3, ChronoUnit.DAYS),
      endsAt = now.plus(10, ChronoUnit.DAYS)
    ))

    // Statusverläufe
    statusService.addInfoStatus(info1a.id, InfoStatus.SCHEDULED, "Vorverkauf gestartet", officer1Id)
    statusService.addInfoStatus(info1b.id, InfoStatus.ACTIVE, "Baustelle eingerichtet", officer1Id)
    statusService.addInfoStatus(info2a.id, InfoStatus.SCHEDULED, "Programm finalisiert", officer2Id)
    statusService.addInfoStatus(info2b.id, InfoStatus.SCHEDULED, "Termin bestätigt", officer2Id)

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

    val (t1PubLat, t1PubLon) = loc(0.002, 0.002)
    val ticketCit1Pub = ticketRepo.create(TicketCreateData(
      title = "Max: Parkbank defekt (Hof)",
      description = "Am Saale-Radweg ist eine Bank zusammengebrochen. Öffentlich sichtbar.",
      category = TicketCategory.INFRASTRUCTURE,
      officeId = office1Id,
      creatorUserId = citizen1Id,
      address = AddressDto(street = "Saale-Radweg", houseNumber = "", zipCode = "95028", city = "Hof", longitude = t1PubLon, latitude = t1PubLat),
      visibility = TicketVisibility.PUBLIC
    ))

    val (t1PrivLat, t1PrivLon) = loc(0.175, 0.198)
    val ticketCit1Priv = ticketRepo.create(TicketCreateData(
      title = "Max: Müllmeldung (Plauen)",
      description = "Privates Ticket über wilde Müllkippe am Stadtrand. Sollte nur für Max und Office 2 sichtbar sein.",
      category = TicketCategory.CLEANING,
      officeId = office2Id,
      creatorUserId = citizen1Id,
      address = AddressDto(street = "Haselbrunner Str.", houseNumber = "100", zipCode = "08525", city = "Plauen", longitude = t1PrivLon, latitude = t1PrivLat),
      visibility = TicketVisibility.PRIVATE
    ))

    val (t2PrivLat, t2PrivLon) = loc(-0.010, -0.010)
    val ticketCit2Priv = ticketRepo.create(TicketCreateData(
      title = "Erika: Lärmbelästigung (Hof)",
      description = "Beschwerde über nächtlichen Lärm. Privat an Office 1.",
      category = TicketCategory.OTHER,
      officeId = office1Id,
      creatorUserId = citizen2Id,
      address = AddressDto(street = "Wunsiedler Str.", houseNumber = "50", zipCode = "95032", city = "Hof", longitude = t2PrivLon, latitude = t2PrivLat),
      visibility = TicketVisibility.PRIVATE
    ))

    val (t2PubLat, t2PubLon) = loc(0.165, 0.190)
    val ticketCit2Pub = ticketRepo.create(TicketCreateData(
      title = "Erika: Schlagloch (Plauen)",
      description = "Großes Schlagloch in der Fahrbahnmitte. Öffentlich.",
      category = TicketCategory.INFRASTRUCTURE,
      officeId = office2Id,
      creatorUserId = citizen2Id,
      address = AddressDto(street = "Oelsnitzer Str.", houseNumber = "20", zipCode = "08527", city = "Plauen", longitude = t2PubLon, latitude = t2PubLat),
      visibility = TicketVisibility.PUBLIC
    ))


    // Ticket-Status
    statusService.addTicketStatus(ticketCit1Pub.id, TicketStatus.OPEN, "Eingegangen", citizen1Id)
    statusService.addTicketStatus(ticketCit1Priv.id, TicketStatus.OPEN, "Eingegangen", citizen1Id)
    statusService.addTicketStatus(ticketCit2Priv.id, TicketStatus.OPEN, "Eingegangen", citizen2Id)
    statusService.addTicketStatus(ticketCit2Pub.id, TicketStatus.OPEN, "Eingegangen", citizen2Id)
    statusService.addTicketStatus(ticketCit2Pub.id, TicketStatus.IN_PROGRESS, "Bauhof informiert", officer2Id)

    // Vote
    ticketRepo.addVote(ticketCit1Pub.id, citizen2Id)

    try {
      seedMedia(MediaTargetType.TICKET, ticketCit1Pub.id, listOf(MediaSeed("istockphoto-playground-1.jpg", true, "image/jpeg")))
      seedMedia(MediaTargetType.TICKET, ticketCit2Priv.id, listOf(MediaSeed("istockphoto-playground-2.jpg", true, "image/jpeg")))
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