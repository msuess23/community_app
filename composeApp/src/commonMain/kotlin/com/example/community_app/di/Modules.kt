package com.example.community_app.di

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
  // Core
  single { HttpClientFactory.create(get()) }
  single { get<AppDatabase>().infoDao() }

  singleOf(::DefaultSettingsRepository).bind<SettingsRepository>()

  // Info Data & Repo
  singleOf(::KtorRemoteInfoDataSource).bind<RemoteInfoDataSource>()
  singleOf(::DefaultInfoRepository).bind<InfoRepository>()

  // Media Data & Repo
  singleOf(::KtorRemoteMediaDataSource).bind<RemoteMediaDataSource>()
  singleOf(::DefaultMediaRepository).bind<MediaRepository>()

  // Info VM
  viewModelOf(::InfoMasterViewModel)
  viewModelOf(::InfoDetailViewModel)

  viewModelOf(::SettingsViewModel)
}