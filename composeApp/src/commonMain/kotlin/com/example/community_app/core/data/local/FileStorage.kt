package com.example.community_app.core.data.local

expect class FileStorage {
  suspend fun saveImage(bytes: ByteArray): String
  suspend fun readImage(path: String): ByteArray?
  suspend fun deleteImage(path: String)
}