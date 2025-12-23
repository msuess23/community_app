package com.example.community_app.core.domain.usecase

import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.notification.NotificationService
import com.example.community_app.info.domain.repository.InfoRepository
import com.example.community_app.settings.domain.repository.SettingsRepository
import com.example.community_app.ticket.domain.repository.TicketRepository
import com.example.community_app.util.InfoStatus
import com.example.community_app.util.TicketStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.notification_desc
import community_app.composeapp.generated.resources.notification_title
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString

class CheckStatusUpdatesUseCase(
  private val ticketRepository: TicketRepository,
  private val infoRepository: InfoRepository,
  private val settingsRepository: SettingsRepository,
  private val notificationService: NotificationService
) {

  suspend operator fun invoke() = coroutineScope {
    val settings = settingsRepository.settings.first()
    if (!settings.notificationsEnabled) return@coroutineScope

    val jobs = mutableListOf<kotlinx.coroutines.Deferred<Unit>>()

    if (settings.notifyTickets) {
      jobs += async { checkTickets() }
    }

    if (settings.notifyInfos) {
      jobs += async { checkInfos() }
    }

    jobs.awaitAll()
  }

  private suspend fun checkTickets() {
    val favorites = ticketRepository.getTickets().first().filter { it.isFavorite }

    val myTickets = try {
      ticketRepository.getUserTickets(-1).first()
    } catch (e: Exception) { emptyList() }

    val uniqueTickets = (favorites + myTickets).distinctBy { it.id }

    uniqueTickets.forEach { localTicket ->
      val result = ticketRepository.getCurrentStatus(localTicket.id)

      if (result is Result.Success) {
        val currentRemoteDto = result.data

        if (currentRemoteDto != null) {
          val remoteStatus = try {
            TicketStatus.valueOf(currentRemoteDto.status.toString())
          } catch(e:Exception) { null }

          if (remoteStatus != null && remoteStatus != localTicket.currentStatus) {
            ticketRepository.refreshTicket(localTicket.id)

            notificationService.showNotification(
              id = localTicket.id,
              title = getString(Res.string.notification_title) + localTicket.title,
              message = getString(Res.string.notification_desc) + remoteStatus
            )
          }
        }
      }
    }
  }

  private suspend fun checkInfos() {
    val favorites = infoRepository.getInfos().first().filter { it.isFavorite }

    favorites.forEach { localInfo ->
      val result = infoRepository.getCurrentStatus(localInfo.id)

      if (result is Result.Success) {
        val currentRemoteDto = result.data

        if (currentRemoteDto != null) {
          val remoteStatus = try {
            InfoStatus.valueOf(currentRemoteDto.status.toString())
          } catch(e:Exception) { null }

          if (remoteStatus != null && remoteStatus != localInfo.currentStatus) {
            infoRepository.refreshInfo(localInfo.id)

            notificationService.showNotification(
              id = localInfo.id * -1,
              title = getString(Res.string.notification_title) + localInfo.title,
              message = getString(Res.string.notification_desc) + remoteStatus
            )
          }
        }
      }
    }
  }
}