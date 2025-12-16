package com.example.community_app.di

import com.example.community_app.appointment.data.local.AppointmentDao
import com.example.community_app.appointment.data.network.KtorRemoteAppointmentDataSource
import com.example.community_app.appointment.data.network.RemoteAppointmentDataSource
import com.example.community_app.appointment.data.repository.DefaultAppointmentRepository
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.appointment.domain.usecase.BookAppointmentUseCase
import com.example.community_app.appointment.domain.usecase.CancelAppointmentUseCase
import com.example.community_app.appointment.domain.usecase.GetAppointmentDetailsUseCase
import com.example.community_app.appointment.domain.usecase.GetFreeSlotsUseCase
import com.example.community_app.appointment.domain.usecase.ObserveAppointmentsUseCase
import com.example.community_app.appointment.domain.usecase.ScheduleAppointmentRemindersUseCase
import com.example.community_app.appointment.presentation.detail.AppointmentDetailViewModel
import com.example.community_app.appointment.presentation.master.AppointmentMasterViewModel
import com.example.community_app.auth.data.network.KtorRemoteAuthDataSource
import com.example.community_app.auth.data.network.RemoteAuthDataSource
import com.example.community_app.auth.data.repository.DefaultAuthRepository
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.presentation.forgot_password.ForgotPasswordViewModel
import com.example.community_app.auth.presentation.login.LoginViewModel
import com.example.community_app.auth.presentation.register.RegisterViewModel
import com.example.community_app.auth.presentation.reset_password.ResetPasswordViewModel
import com.example.community_app.core.data.HttpClientFactory
import com.example.community_app.core.data.local.AppDatabase
import com.example.community_app.core.data.permission.MokoPermissionService
import com.example.community_app.core.domain.permission.AppPermissionService
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.auth.domain.usecase.IsUserLoggedInUseCase
import com.example.community_app.core.data.local.favorite.FavoriteDao
import com.example.community_app.core.data.sync.SyncManager
import com.example.community_app.core.domain.usecase.CheckStatusUpdatesUseCase
import com.example.community_app.core.domain.usecase.ToggleFavoriteUseCase
import com.example.community_app.info.data.local.InfoDao
import com.example.community_app.info.data.network.KtorRemoteInfoDataSource
import com.example.community_app.info.data.network.RemoteInfoDataSource
import com.example.community_app.info.data.repository.DefaultInfoRepository
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.info.domain.usecase.FilterInfosUseCase
import com.example.community_app.info.domain.usecase.ObserveInfosUseCase
import com.example.community_app.info.presentation.info_detail.InfoDetailViewModel
import com.example.community_app.info.presentation.info_master.InfoMasterViewModel
import com.example.community_app.media.data.network.KtorRemoteMediaDataSource
import com.example.community_app.media.data.network.RemoteMediaDataSource
import com.example.community_app.media.data.repository.DefaultMediaRepository
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.office.data.local.OfficeDao
import com.example.community_app.office.data.network.KtorRemoteOfficeDataSource
import com.example.community_app.office.data.network.RemoteOfficeDataSource
import com.example.community_app.office.data.repository.DefaultOfficeRepository
import com.example.community_app.office.domain.OfficeRepository
import com.example.community_app.office.domain.usecase.FilterOfficesUseCase
import com.example.community_app.office.domain.usecase.ObserveOfficesUseCase
import com.example.community_app.office.presentation.office_detail.OfficeDetailViewModel
import com.example.community_app.office.presentation.office_master.OfficeMasterViewModel
import com.example.community_app.profile.data.local.UserDao
import com.example.community_app.profile.data.network.KtorRemoteUserDataSource
import com.example.community_app.profile.data.network.RemoteUserDataSource
import com.example.community_app.profile.data.repository.DefaultUserRepository
import com.example.community_app.profile.domain.UserRepository
import com.example.community_app.profile.presentation.ProfileViewModel
import com.example.community_app.settings.data.DefaultSettingsRepository
import com.example.community_app.settings.domain.SettingsRepository
import com.example.community_app.settings.presentation.SettingsViewModel
import com.example.community_app.ticket.data.local.draft.TicketDraftDao
import com.example.community_app.ticket.data.local.ticket.TicketDao
import com.example.community_app.ticket.data.network.KtorRemoteTicketDataSource
import com.example.community_app.ticket.data.network.RemoteTicketDataSource
import com.example.community_app.ticket.data.repository.DefaultTicketRepository
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.ticket.domain.usecase.VoteTicketUseCase
import com.example.community_app.ticket.domain.usecase.detail.SyncTicketImagesUseCase
import com.example.community_app.ticket.domain.usecase.edit.AddLocalImageUseCase
import com.example.community_app.ticket.domain.usecase.edit.DeleteTicketDataUseCase
import com.example.community_app.ticket.domain.usecase.edit.GetTicketEditDetailsUseCase
import com.example.community_app.ticket.domain.usecase.edit.UpdateTicketUseCase
import com.example.community_app.ticket.domain.usecase.master.FilterTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveCommunityTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveMyTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveTicketsUseCase
import com.example.community_app.ticket.presentation.ticket_detail.TicketDetailViewModel
import com.example.community_app.ticket.presentation.ticket_edit.TicketEditViewModel
import com.example.community_app.ticket.presentation.ticket_master.TicketMasterViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
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

  single<AppPermissionService> { MokoPermissionService(get()) }


  // --- DATA SOURCES ---
  single<RemoteAuthDataSource> {
    KtorRemoteAuthDataSource(httpClient = get(named("publicClient")))
  }
  single<RemoteInfoDataSource> {
    KtorRemoteInfoDataSource(httpClient = get(named("publicClient")))
  }
  single<RemoteTicketDataSource> {
    KtorRemoteTicketDataSource(httpClient = get(named("authClient")))
  }
  single<RemoteMediaDataSource> {
    KtorRemoteMediaDataSource(httpClient = get(named("authClient")))
  }
  single<RemoteOfficeDataSource> {
    KtorRemoteOfficeDataSource(httpClient = get(named("publicClient")))
  }
  single<RemoteAppointmentDataSource> {
    KtorRemoteAppointmentDataSource(httpClient = get(named("authClient")))
  }
  single<RemoteUserDataSource> {
    KtorRemoteUserDataSource(httpClient = get(named("authClient")))
  }


  // --- REPOSITORIES ---
  single<InfoDao> { get<AppDatabase>().infoDao() }
  single<TicketDao> { get<AppDatabase>().ticketDao() }
  single<TicketDraftDao> { get<AppDatabase>().ticketDraftDao() }
  single<OfficeDao> { get<AppDatabase>().officeDao() }
  single<AppointmentDao> { get<AppDatabase>().appointmentDao() }
  single<FavoriteDao> { get<AppDatabase>().favoriteDao() }
  single<UserDao> { get<AppDatabase>().userDao() }

  singleOf(::SyncManager)

  singleOf(::DefaultAuthRepository).bind<AuthRepository>()
  singleOf(::DefaultSettingsRepository).bind<SettingsRepository>()
  singleOf(::DefaultInfoRepository).bind<InfoRepository>()
  singleOf(::DefaultTicketRepository).bind<TicketRepository>()
  singleOf(::DefaultMediaRepository).bind<MediaRepository>()
  singleOf(::DefaultOfficeRepository).bind<OfficeRepository>()
  singleOf(::DefaultAppointmentRepository).bind<AppointmentRepository>()
  singleOf(::DefaultUserRepository).bind<UserRepository>()

  // --- USE CASES ---
  factoryOf(::IsUserLoggedInUseCase)
  factoryOf(::FetchUserLocationUseCase)
  factoryOf(::ToggleFavoriteUseCase)

  factoryOf(::ObserveInfosUseCase)
  factoryOf(::FilterInfosUseCase)

  factoryOf(::ObserveTicketsUseCase)
  factoryOf(::ObserveCommunityTicketsUseCase)
  factoryOf(::ObserveMyTicketsUseCase)
  factoryOf(::FilterTicketsUseCase)
  factoryOf(::SyncTicketImagesUseCase)
  factoryOf(::AddLocalImageUseCase)
  factoryOf(::DeleteTicketDataUseCase)
  factoryOf(::GetTicketEditDetailsUseCase)
  factoryOf(::UpdateTicketUseCase)
  factoryOf(::VoteTicketUseCase)

  factoryOf(::ObserveOfficesUseCase)
  factoryOf(::FilterOfficesUseCase)

  factoryOf(::GetFreeSlotsUseCase)
  factoryOf(::BookAppointmentUseCase)
  factoryOf(::ObserveAppointmentsUseCase)
  factoryOf(::GetAppointmentDetailsUseCase)
  factoryOf(::CancelAppointmentUseCase)
  factoryOf(::CheckStatusUpdatesUseCase)
  factoryOf(::ScheduleAppointmentRemindersUseCase)

  // --- VIEW MODELS ---
  viewModelOf(::LoginViewModel)
  viewModelOf(::RegisterViewModel)
  viewModelOf(::ForgotPasswordViewModel)
  viewModelOf(::ResetPasswordViewModel)
  viewModelOf(::SettingsViewModel)
  viewModelOf(::InfoMasterViewModel)
  viewModelOf(::InfoDetailViewModel)
  viewModelOf(::TicketMasterViewModel)
  viewModelOf(::TicketDetailViewModel)
  viewModelOf(::TicketEditViewModel)
  viewModelOf(::OfficeMasterViewModel)
  viewModelOf(::OfficeDetailViewModel)
  viewModelOf(::AppointmentMasterViewModel)
  viewModelOf(::AppointmentDetailViewModel)
  viewModelOf(::ProfileViewModel)
}