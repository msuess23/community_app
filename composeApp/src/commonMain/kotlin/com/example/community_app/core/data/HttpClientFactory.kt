package com.example.community_app.core.data

import com.example.community_app.config.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
  fun create(
    engine: HttpClientEngine,
    tokenProvider: (suspend () -> String?)? = null
  ): HttpClient {
    return HttpClient(engine) {
      install(ContentNegotiation) {
        json(AppJson)
      }

      if (tokenProvider != null) {
        install(Auth) {
          bearer {
            loadTokens {
              val token = tokenProvider()
              if (token != null) {
                BearerTokens(token, "")
              } else null
            }
          }
        }
      }

      install(HttpTimeout) {
        socketTimeoutMillis = 20_000L
        requestTimeoutMillis = 20_000
      }

      install(Logging) {
        logger = object : Logger {
          override fun log(message: String) {
            println(message)
          }
        }
        level = LogLevel.INFO
      }

      defaultRequest {
        contentType(ContentType.Application.Json)
      }
    }
  }
}