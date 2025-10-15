package com.example.community_app

import com.example.community_app.config.*
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main() {
  embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
    .start(wait = true)
}

fun Application.module() {
  val baseDir = System.getProperty("user.dir")
  val configFile = File("$baseDir/src/main/resources/application.conf")

  val config = if (configFile.exists()) {
    log.info("Manually loading config from: ${configFile.absolutePath}")
    HoconApplicationConfig(ConfigFactory.parseFile(configFile))
  } else {
    log.warn("application.conf not found at ${configFile.absolutePath} — using default environment config")
    environment.config
  }

  configureSerialization()
  configureCors()
  configureExceptionHandling()

  JwtConfig.init(config)
  DatabaseConfig.init(config)
  MediaConfig.init(config)
  DatabaseSeeder.runIfEnabled(config)

  configureSecurity()
  configureRouting()
}