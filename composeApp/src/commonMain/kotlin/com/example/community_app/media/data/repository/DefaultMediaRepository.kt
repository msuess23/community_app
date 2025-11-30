package com.example.community_app.media.data.repository

import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map
import com.example.community_app.dto.MediaDto
import com.example.community_app.media.data.network.RemoteMediaDataSource
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.util.MediaTargetType

class DefaultMediaRepository(
  private val remoteDataSource: RemoteMediaDataSource,
  private val fileStorage: FileStorage
) : MediaRepository {
  override suspend fun getMediaList(
    targetType: MediaTargetType,
    targetId: Int
  ): Result<List<MediaDto>, DataError.Remote> {
    return remoteDataSource.getMediaList(targetType, targetId)
  }

  override suspend fun downloadMedia(url: String, saveToFileName: String): Result<String, DataError.Remote> {
    return when (val result = remoteDataSource.downloadMedia(url)) {
      is Result.Success -> {
        try {
          fileStorage.saveFile(saveToFileName, result.data)
          Result.Success(saveToFileName)
        } catch (e: Exception) {
          Result.Error(DataError.Remote.UNKNOWN)
        }
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun uploadMedia(
    targetType: MediaTargetType,
    targetId: Int,
    fileName: String
  ): Result<MediaDto, DataError.Remote> {
    val fileSize = fileStorage.getFileSize(fileName)
    if (fileSize == 0L) {
      println("Upload failed: File $fileName has size 0 or not found")
      return Result.Error(DataError.Remote.UNKNOWN)
    }

    val inputProvider = {
      fileStorage.readFileAsInput(fileName) ?: error("File not found: $fileName")
    }

    return remoteDataSource.uploadMedia(
      targetType = targetType,
      targetId = targetId,
      inputProvider = inputProvider,
      fileName = fileName,
      contentLength = fileSize
    )
  }

  override suspend fun deleteMedia(
    targetType: MediaTargetType,
    targetId: Int,
    mediaId: Int
  ): Result<Unit, DataError.Remote> {
    return remoteDataSource.deleteMedia(targetType, targetId, mediaId)
  }

  override suspend fun setCover(mediaId: Int): Result<MediaDto, DataError.Remote> {
    return remoteDataSource.setCover(mediaId)
  }
}