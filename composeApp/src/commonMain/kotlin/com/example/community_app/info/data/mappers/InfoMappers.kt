package com.example.community_app.info.data.mappers

import com.example.community_app.info.data.dto.InfoDto
import com.example.community_app.info.domain.Info

fun InfoDto.toInfo(): Info {
  return Info(
    id = id,
    title = title,
    description = description ?: "",
    officeId = officeId,
    createdAt = createdAt,
    startsAt = startsAt,
    endsAt = endsAt,
    imageUrl = imageUrl ?: ""
  )
}