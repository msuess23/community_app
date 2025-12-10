package com.example.community_app.appointment.presentation.master.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatIsoTime
import compose.icons.FeatherIcons
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Clock

@Composable
fun AppointmentListItem(
  appointment: Appointment,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(Spacing.medium),
    color = MaterialTheme.colorScheme.surfaceContainer,
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier.padding(Spacing.medium),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Date Box Icon
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
          .padding(12.dp)
      ) {
        Icon(
          imageVector = FeatherIcons.Calendar,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }

      Spacer(modifier = Modifier.width(Spacing.medium))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = formatIsoDate(appointment.startsAt),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = FeatherIcons.Clock,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.secondary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${formatIsoTime(appointment.startsAt)} - ${formatIsoTime(appointment.endsAt)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }

      Icon(
        imageVector = FeatherIcons.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}