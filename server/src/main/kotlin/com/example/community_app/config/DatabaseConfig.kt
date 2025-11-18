package com.example.community_app.config

import com.example.community_app.model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Paths

object DatabaseConfig {
  fun init(config: ApplicationConfig) {
    val dbConfig = config.config("ktor.database")
    Database.connect(
      url = dbConfig.property("url").getString(),
      driver = dbConfig.property("driver").getString(),
      user = dbConfig.property("user").getString(),
      password = dbConfig.property("password").getString()
    )

    transaction {
      SchemaUtils.create(Users, Settings, Addresses, Offices, Appointments, Infos, StatusEntries, Tickets, TicketVotes, Media)
    }
  }
}