package com.example.community_app.config

import com.example.community_app.model.*
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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
      SchemaUtils.drop(Users, Settings, Locations, Offices, Appointments)
      SchemaUtils.create(Users, Settings, Locations, Offices, Appointments)
    }
  }
}