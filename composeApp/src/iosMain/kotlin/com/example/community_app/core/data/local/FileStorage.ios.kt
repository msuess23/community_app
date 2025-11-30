package com.example.community_app.core.data.local

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readAvailable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.posix.uint8_tVar

actual class FileStorage {
  private val fileManager = NSFileManager.defaultManager

  @OptIn(ExperimentalForeignApi::class)
  private fun getDocumentsDirectory(): String {
    val paths = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val url = paths.first() as NSURL
    val dir = url.path + "/images"

    if (!fileManager.fileExistsAtPath(dir)) {
      fileManager.createDirectoryAtPath(dir, true, null, null)
    }
    return dir
  }

  @OptIn(ExperimentalForeignApi::class)
  actual suspend fun moveFromTemp(sourcePath: String): String = withContext(Dispatchers.IO) {
    val fileName = "img_${NSUUID().UUIDString()}.jpg"
    val destPath = "${getDocumentsDirectory()}/$fileName"

    val success = fileManager.moveItemAtPath(sourcePath, destPath, null)
    if (!success) {
      fileManager.copyItemAtPath(sourcePath, destPath, null)
      fileManager.removeItemAtPath(sourcePath, null)
    }
    fileName
  }

  @OptIn(ExperimentalForeignApi::class)
  actual suspend fun saveFile(
    fileName: String,
    channel: ByteReadChannel
  ): String = withContext(Dispatchers.IO) {
    val fullPath = "${getDocumentsDirectory()}/$fileName"
    val url = NSURL.fileURLWithPath(fullPath)

    val outputStream = NSOutputStream(uRL = url, append = false)
    outputStream.open()

    val bufferSize = 16 * 1024
    val buffer = ByteArray(bufferSize)

    try {
      while (!channel.isClosedForRead) {
        val read = channel.readAvailable(buffer, 0, bufferSize)
        if (read == -1) break
        if (read > 0) {
          buffer.usePinned { pinned ->
            val ptr = pinned.addressOf(0).reinterpret<uint8_tVar>()
            outputStream.write(ptr, read.toULong())
          }
        }
      }
    } finally {
      outputStream.close()
    }

    fileName
  }

  @OptIn(ExperimentalForeignApi::class)
  actual fun readFileAsInput(fileName: String): Input? {
    val fullPath = "${getDocumentsDirectory()}/$fileName"
    if (!fileManager.fileExistsAtPath(fullPath)) return null

    val data = NSData.dataWithContentsOfFile(fullPath) ?: return null
    val length = data.length.toInt()
    val bytes = ByteArray(length)

    if (length > 0) {
      bytes.usePinned { pinned ->
        data.getBytes(pinned.addressOf(0), data.length)
      }
    }

    return buildPacket {
      writeFully(bytes)
    }
  }

  @OptIn(ExperimentalForeignApi::class)
  actual fun getFileSize(fileName: String): Long {
    val fullPath = "${getDocumentsDirectory()}/$fileName"
    if (!fileManager.fileExistsAtPath(fullPath)) return 0L

    val attrs = fileManager.attributesOfItemAtPath(fullPath, null)
    return (attrs?.get(NSFileSize) as? Long) ?: 0L
  }

  @OptIn(ExperimentalForeignApi::class)
  actual suspend fun deleteImage(fileName: String) = withContext(Dispatchers.IO) {
    val fullPath = getFullPath(fileName)
    if (fileManager.fileExistsAtPath(fullPath)) {
      fileManager.removeItemAtPath(fullPath, null)
    }
    Unit
  }

  actual fun exists(fileName: String): Boolean {
    return fileManager.fileExistsAtPath("${getDocumentsDirectory()}/$fileName")
  }

  actual fun getFullPath(fileName: String): String {
    return "${getDocumentsDirectory()}/$fileName"
  }
}