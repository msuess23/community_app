package com.example.community_app.di

import com.example.community_app.core.data.HttpClientFactory
import com.example.community_app.core.data.local.AppDatabase
import com.example.community_app.info.data.network.KtorRemoteInfoDataSource
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.data.repository.DefaultInfoRepository
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.info.presentation.info_detail.InfoDetailViewModel
import com.example.community_app.info.presentation.info_master.InfoMasterViewModel
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
  single { HttpClientFactory.create(get()) }

  single { get<AppDatabase>().infoDao() }

  singleOf(::KtorRemoteInfoDataSource).bind<RemoteInfoDataSource>()
  singleOf(::DefaultInfoRepository).bind<InfoRepository>()

  viewModelOf(::InfoMasterViewModel)
  viewModelOf(::InfoDetailViewModel)
}