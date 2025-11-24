package com.example.community_app.di

import com.example.community_app.auth.data.network.KtorRemoteAuthDataSource
import com.example.community_app.auth.data.network.RemoteAuthDataSource
import com.example.community_app.auth.data.repository.DefaultAuthRepository
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.presentation.login.LoginViewModel
import com.example.community_app.core.data.HttpClientFactory
import com.example.community_app.core.data.local.AppDatabase
import com.example.community_app.info.data.network.KtorRemoteInfoDataSource
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.data.repository.DefaultInfoRepository
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.info.presentation.info_detail.InfoDetailViewModel
import com.example.community_app.info.presentation.info_master.InfoMasterViewModel
import com.example.community_app.media.data.network.KtorRemoteMediaDataSource
import com.example.community_app.media.data.network.RemoteMediaDataSource
import com.example.community_app.media.data.repository.DefaultMediaRepository
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.settings.data.DefaultSettingsRepository
import com.example.community_app.settings.domain.SettingsRepository
import com.example.community_app.settings.presentation.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

fun createKoinConfiguration(): KoinConfiguration {
  return KoinConfiguration {
    modules(platformModule, sharedModule)
  }
}

val sharedModule = module {
  // --- HTTP Clients ---
  single(named("publicClient")) {
    HttpClientFactory.create(engine = get())
  }
  single(named("authClient")) {
    val authRepo = get<AuthRepository>()
    HttpClientFactory.create(
      engine = get(),
      tokenProvider = { authRepo.getAccessToken() }
    )
  }


  // --- DATA SOURCES ---
  single<RemoteAuthDataSource> {
    KtorRemoteAuthDataSource(httpClient = get(named("publicClient")))
  }
  single<RemoteInfoDataSource> {
    KtorRemoteInfoDataSource(httpClient = get(named("publicClient")))
  }
  single<RemoteMediaDataSource> {
    KtorRemoteMediaDataSource(httpClient = get(named("authClient")))
  }


  // --- REPOSITORIES ---
  single { get<AppDatabase>().infoDao() }

  singleOf(::DefaultAuthRepository).bind<AuthRepository>()
  singleOf(::DefaultSettingsRepository).bind<SettingsRepository>()
  singleOf(::DefaultInfoRepository).bind<InfoRepository>()
  singleOf(::DefaultMediaRepository).bind<MediaRepository>()


  // --- VIEW MODELS ---
  viewModelOf(::LoginViewModel)
  viewModelOf(::SettingsViewModel)
  viewModelOf(::InfoMasterViewModel)
  viewModelOf(::InfoDetailViewModel)
}