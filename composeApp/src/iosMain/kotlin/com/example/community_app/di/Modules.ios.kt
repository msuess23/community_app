package com.example.community_app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.community_app.core.data.local.AppDatabase
import com.example.community_app.core.data.local.createDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule = module {
  single<HttpClientEngine> { Darwin.create() }

  single<AppDatabase> {
    val dbFilePath = documentDirectory() + "/community.db"

    Room.databaseBuilder<AppDatabase>(
      name = dbFilePath
    )
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .fallbackToDestructiveMigrationOnDowngrade(true)
      .fallbackToDestructiveMigration(true)
      .build()
  }

  single<DataStore<Preferences>> {
    createDataStore()
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
  val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
    directory = NSDocumentDirectory,
    inDomain = NSUserDomainMask,
    appropriateForURL = null,
    create = false,
    error = null,
  )
  return requireNotNull(documentDirectory?.path) { "Document directory not found" }
}