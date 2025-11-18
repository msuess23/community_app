package com.example.community_app.util

import com.example.community_app.dto.AddressDto
import com.example.community_app.repository.AddressRecord

fun AddressRecord.toDto(): AddressDto =
  AddressDto(
    street = street,
    houseNumber = houseNumber,
    zipCode = zipCode,
    city = city,
    longitude = longitude,
    latitude  = latitude
  )
