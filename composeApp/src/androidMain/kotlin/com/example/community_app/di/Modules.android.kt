package com.example.community_app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.community_app.core.data.local.AppDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
  single<HttpClientEngine> { OkHttp.create() }

  single<AppDatabase> {
    val context = androidContext()
    val dbFile = context.getDatabasePath("community.db")

    Room.databaseBuilder<AppDatabase>(
      context = context,
      name = dbFile.absolutePath
    )
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .fallbackToDestructiveMigrationOnDowngrade(true)
      .fallbackToDestructiveMigration(true)
      .build()
  }
}