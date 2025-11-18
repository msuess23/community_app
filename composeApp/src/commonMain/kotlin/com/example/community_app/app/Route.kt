package com.example.community_app.app

import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable
  data object InfoGraph: Route

  @Serializable
  data object InfoMaster: Route

  @Serializable
  data class InfoDetail(val id: Int): Route

}