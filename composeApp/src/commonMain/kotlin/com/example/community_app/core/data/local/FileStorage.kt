package com.example.community_app.core.data.local

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.Input

expect class FileStorage {
  suspend fun moveFromTemp(sourcePath: String): String
  suspend fun saveFile(fileName: String, channel: ByteReadChannel): String
  fun readFileAsInput(fileName: String): Input?
  fun getFileSize(fileName: String): Long
  suspend fun deleteImage(fileName: String)

  fun exists(fileName: String): Boolean
  fun getFullPath(fileName: String): String
}