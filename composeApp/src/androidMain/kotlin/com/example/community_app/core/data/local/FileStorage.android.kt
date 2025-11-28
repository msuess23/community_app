package com.example.community_app.core.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

actual class FileStorage(private val context: Context) {
  actual suspend fun saveImage(bytes: ByteArray): String = withContext(Dispatchers.IO) {
    val fileName = "draft_${UUID.randomUUID()}.jpg"
    val directory = File(context.filesDir, "drafts")
    if (!directory.exists()) directory.mkdirs()

    val file = File(directory, fileName)
    file.writeBytes(bytes)
    file.absolutePath
  }

  actual suspend fun readImage(path: String): ByteArray? = withContext(Dispatchers.IO) {
    val file = File(path)
    if (file.exists()) file.readBytes() else null
  }

  actual suspend fun deleteImage(path: String) = withContext(Dispatchers.IO) {
    val file = File(path)
    if (file.exists()) {
      file.delete()
    }
    Unit
  }
}