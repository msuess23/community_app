package com.example.community_app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.community_app.core.data.local.AppDatabase
import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.data.local.createDataStore
import com.example.community_app.core.domain.calendar.AndroidCalendarManager
import com.example.community_app.core.domain.calendar.CalendarManager
import com.example.community_app.core.domain.location.AndroidLocationService
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.domain.notification.AndroidNotificationService
import com.example.community_app.core.domain.notification.NotificationService
import com.example.community_app.core.domain.permission.AndroidCalendarPermissionService
import com.example.community_app.core.domain.permission.CalendarPermissionService
import dev.icerock.moko.permissions.PermissionsController
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

  single<DataStore<Preferences>> {
    createDataStore(context = androidContext())
  }

  single<LocationService> {
    AndroidLocationService(androidContext())
  }

  single<CalendarPermissionService> {
    AndroidCalendarPermissionService(androidContext())
  }

  single<CalendarManager> {
    AndroidCalendarManager(androidContext())
  }

  single { PermissionsController(applicationContext = androidContext()) }

  single { FileStorage(androidContext()) }

  single<NotificationService> {
    AndroidNotificationService(androidContext())
  }
}