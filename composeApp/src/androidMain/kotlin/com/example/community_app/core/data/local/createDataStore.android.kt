package com.example.community_app.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private var dataStore: DataStore<Preferences>? = null

fun createDataStore(context: Context): DataStore<Preferences> {
 return dataStore ?: synchronized(Any()) {
   dataStore ?: createDataStore(
     producePath = { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
   ).also { dataStore = it }
 }
}