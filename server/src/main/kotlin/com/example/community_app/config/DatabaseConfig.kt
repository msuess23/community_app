package com.example.community_app.config

import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
  fun init(dbConfig: ApplicationConfig) {
    Database.connect(
      url = dbConfig.property("ktor.database.url").getString(),
      driver = dbConfig.property("ktor.database.driver").getString(),
      user = dbConfig.property("ktor.database.user").getString(),
      password = dbConfig.property("ktor.database.password").getString()
    )

    transaction {}
  }
}