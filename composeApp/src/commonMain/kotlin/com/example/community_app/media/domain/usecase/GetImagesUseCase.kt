package com.example.community_app.media.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.media.domain.repository.MediaRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType

class GetImagesUseCase(
  private val mediaRepository: MediaRepository
) {
  suspend operator fun invoke(
    targetType: MediaTargetType,
    targetId: Int
  ): Result<List<String>, DataError> {
    return when(val result = mediaRepository.getMediaList(targetType, targetId)) {
      is Result.Success -> {
        val fullUrls = result.data.map { imageDto ->
          "$BASE_URL${imageDto.url}"
        }
        Result.Success(fullUrls)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }
}