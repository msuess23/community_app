package com.example.community_app.profile.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.AddressRepository
import com.example.community_app.profile.domain.User
import com.example.community_app.profile.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

data class ProfileData(
  val user: User?,
  val homeAddress: Address?,
  val syncError: DataError? = null
)

class GetProfileDataUseCase(
  private val userRepository: UserRepository,
  private val addressRepository: AddressRepository
) {
  operator fun invoke(): Flow<Result<ProfileData, DataError>> = flow {
    val refreshResult = userRepository.refreshUser()
    val syncError = (refreshResult as? Result.Error)?.error

    emitAll(
      combine(
        userRepository.getUser(),
        addressRepository.getHomeAddress()
      ) { user, address ->
        Result.Success(
          ProfileData(
            user = user,
            homeAddress = address,
            syncError = syncError
          )
        )
      }
    )
  }
}