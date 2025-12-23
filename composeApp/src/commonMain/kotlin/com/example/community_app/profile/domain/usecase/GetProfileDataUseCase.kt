package com.example.community_app.profile.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.geocoding.domain.repository.AddressRepository
import com.example.community_app.profile.domain.model.User
import com.example.community_app.profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
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
    val currentUser = userRepository.getUser().firstOrNull()

    val syncError = if (currentUser != null) {
      val refreshResult = userRepository.refreshUser()
      (refreshResult as? Result.Error)?.error
    } else null

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