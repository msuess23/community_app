package com.example.community_app.config

import com.example.community_app.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
  routing {
    route("/api") {
      authRoutes()
    }
  }
}
