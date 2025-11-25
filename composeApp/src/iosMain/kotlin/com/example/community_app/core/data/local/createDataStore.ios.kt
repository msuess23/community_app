package com.example.community_app.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

private val dataStore = atomic<DataStore<Preferences>?>(null)

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
  val existing = dataStore.value
  if (existing != null) return existing

  val newInstance = createDataStore(
    producePath = {
      val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
      )
      requireNotNull(documentDirectory).path + "/$DATA_STORE_FILE_NAME"
    }
  )

  dataStore.compareAndSet(null, newInstance)
  return dataStore.value!!
}