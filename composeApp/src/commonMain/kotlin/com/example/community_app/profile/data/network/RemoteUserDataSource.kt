package com.example.community_app.profile.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.UserDto
import com.example.community_app.dto.UserUpdateDto

interface RemoteUserDataSource {
  suspend fun getProfile(): Result<UserDto, DataError.Remote>
  suspend fun updateProfile(request: UserUpdateDto): Result<UserDto, DataError.Remote>
}