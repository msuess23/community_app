package com.example.community_app.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_plural
import community_app.composeapp.generated.resources.info_plural
import community_app.composeapp.generated.resources.office_plural
import community_app.composeapp.generated.resources.settings_label
import community_app.composeapp.generated.resources.ticket_plural
import compose.icons.FeatherIcons
import compose.icons.feathericons.Briefcase
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.Info
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Users
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
  val route: Route,
  val label: StringResource,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val showInBottomBar: Boolean = true,
  val showInDrawer: Boolean = true
) {
  INFOS(
    route = Route.InfoGraph,
    label = Res.string.info_plural,
    selectedIcon = FeatherIcons.Info,
    unselectedIcon = FeatherIcons.Info
  ),
  TICKETS(
    route = Route.TicketGraph,
    label = Res.string.ticket_plural,
    selectedIcon = FeatherIcons.Users,
    unselectedIcon = FeatherIcons.Users
  ),
  OFFICES(
    route = Route.OfficeGraph,
    label = Res.string.office_plural,
    selectedIcon = FeatherIcons.Briefcase,
    unselectedIcon = FeatherIcons.Briefcase
  ),
  APPOINTMENTS(
    route = Route.AppointmentGraph,
    label = Res.string.appointment_plural,
    selectedIcon = FeatherIcons.Calendar,
    unselectedIcon = FeatherIcons.Calendar
  ),
  SETTINGS(
    route = Route.Settings,
    label = Res.string.settings_label,
    selectedIcon = FeatherIcons.Settings,
    unselectedIcon = FeatherIcons.Settings,
    showInBottomBar = false,
    showInDrawer = true
  )
}