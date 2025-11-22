package com.example.community_app.info.data.mappers

import com.example.community_app.dto.InfoDto
import com.example.community_app.info.data.local.InfoAddressEntity
import com.example.community_app.info.data.local.InfoEntity
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoAddress
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus

fun InfoDto.toEntity(): InfoEntity {
  return InfoEntity(
    id = id,
    title = title,
    description = description,
    category = category.toString(),
    officeId = officeId,
    address = address?.let {
      InfoAddressEntity(
        street = it.street,
        houseNumber = it.houseNumber,
        zipCode = it.zipCode,
        city = it.city,
        longitude = it.longitude,
        latitude = it.latitude
      )
    },
    createdAt = createdAt,
    startsAt = startsAt,
    endsAt = endsAt,
    currentStatus = currentStatus?.status?.toString(),
    statusMessage = currentStatus?.message,
    imageUrl = imageUrl
  )
}

fun InfoEntity.toInfo(): Info {
  return Info(
    id = id,
    title = title,
    description = description,
    category = try {
      InfoCategory.valueOf(category)
    } catch (e: Exception) {
      InfoCategory.OTHER
    },
    officeId = officeId,
    address = address?.let {
      InfoAddress(street = it.street, city = it.city)
    },
    createdAt = createdAt,
    startsAt = startsAt,
    endsAt = endsAt,
    currentStatus = currentStatus?.let {
      try { InfoStatus.valueOf(it) } catch(e: Exception) { null }
    },
    statusMessage = statusMessage,
    imageUrl = imageUrl?.let { "$BASE_URL$it" }
  )
}