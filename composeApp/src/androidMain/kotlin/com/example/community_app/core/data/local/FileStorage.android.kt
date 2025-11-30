package com.example.community_app.core.data.local

import android.content.Context
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.streams.asInput
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

actual class FileStorage(private val context: Context) {
  private val imagesDir: File
    get() = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }

  actual suspend fun moveFromTemp(sourcePath: String): String = withContext(Dispatchers.IO) {
    val sourceFile = File(sourcePath)
    val fileName = "img_${UUID.randomUUID()}.jpg"
    val destFile = File(imagesDir, fileName)

    if (!sourceFile.renameTo(destFile)) {
      sourceFile.copyTo(destFile, overwrite = true)
      sourceFile.delete()
    }
    fileName
  }

  actual suspend fun saveFile(
    fileName: String,
    channel: ByteReadChannel): String = withContext(Dispatchers.IO) {
    val file = File(imagesDir, fileName)
    FileOutputStream(file).use { output ->
      channel.copyTo(output)
    }
    fileName
  }

  actual fun readFileAsInput(fileName: String): Input? {
    val file = File(imagesDir, fileName)
    if (!file.exists()) return null
    return FileInputStream(file).asInput()
  }

  actual fun getFileSize(fileName: String): Long {
    val file = File(imagesDir, fileName)
    return if (file.exists()) file.length() else 0L
  }

  actual suspend fun deleteImage(fileName: String) = withContext(Dispatchers.IO) {
    val file = File(imagesDir, fileName)
    if (file.exists()) file.delete()
    Unit
  }

  actual fun exists(fileName: String): Boolean {
    return File(imagesDir, fileName).exists()
  }

  actual fun getFullPath(fileName: String): String {
    return File(imagesDir, fileName).absolutePath
  }
}