package com.example.community_app.config

import com.example.community_app.routes.*
import io.ktor.server.application.*
import io.ktor.server.auth.* // Wichtig für 'authenticate'
import io.ktor.server.routing.*

fun Application.configureRouting() {
  routing {
    route("/api") {
      authRoutes()

      authenticate("auth-jwt", optional = true) {
        userRoutes()
        officeRoutes()
        ticketRoutes()
        infoRoutes()
        mediaRoutes()
        appointmentRoutes()
      }
    }
  }
}
