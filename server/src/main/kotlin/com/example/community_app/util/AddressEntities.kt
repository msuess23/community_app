package com.example.community_app.util

import com.example.community_app.dto.AddressDto
import com.example.community_app.model.AddressEntity

fun createAddress(dto: AddressDto): AddressEntity =
  AddressEntity.new {
    street = dto.street
    houseNumber = dto.houseNumber
    zipCode = dto.zipCode
    city = dto.city
    longitude = dto.longitude
    latitude = dto.latitude
  }

fun AddressEntity.updateFrom(dto: AddressDto) {
  street = dto.street
  houseNumber = dto.houseNumber
  zipCode = dto.zipCode
  city = dto.city
  longitude = dto.longitude
  latitude = dto.latitude
}