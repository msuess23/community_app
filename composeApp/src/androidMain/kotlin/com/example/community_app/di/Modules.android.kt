package com.example.community_app.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual val platformModule = module {
  single<HttpClientEngine> { OkHttp.create() }
}