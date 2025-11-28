package com.example.community_app.core.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes

actual class FileStorage {
  private val fileManager = NSFileManager.defaultManager

  @OptIn(ExperimentalForeignApi::class)
  actual suspend fun saveImage(bytes: ByteArray): String = withContext(Dispatchers.IO) {
    val fileName = "draft_${generateUUID()}.jpg"
    val directory = getDraftsDirectory()

    // Ensure directory exists
    if (!fileManager.fileExistsAtPath(directory)) {
      fileManager.createDirectoryAtPath(directory, true, null, null)
    }

    val filePath = "$directory/$fileName"

    // Convert ByteArray to NSData and write
    val data = bytes.usePinned { pinned ->
      NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
    data.writeToFile(filePath, true)

    filePath
  }

  @OptIn(ExperimentalForeignApi::class)
  actual suspend fun readImage(path: String): ByteArray? = withContext(Dispatchers.IO) {
    if (!fileManager.fileExistsAtPath(path)) return@withContext null

    val data = NSData.dataWithContentsOfFile(path) ?: return@withContext null
    val length = data.length.toInt()
    val bytes = ByteArray(length)

    if (length > 0) {
      bytes.usePinned { pinned ->
        data.getBytes(pinned.addressOf(0), length.toULong())
      }
    }
    bytes
  }

  @OptIn(ExperimentalForeignApi::class)
  actual suspend fun deleteImage(path: String) = withContext(Dispatchers.IO) {
    if (fileManager.fileExistsAtPath(path)) {
      fileManager.removeItemAtPath(path, null)
    }
    Unit
  }

  private fun getDraftsDirectory(): String {
    val paths = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val documentsURL = paths.first() as NSURL
    return documentsURL.path + "/drafts"
  }

  private fun generateUUID(): String = platform.Foundation.NSUUID().UUIDString()
}