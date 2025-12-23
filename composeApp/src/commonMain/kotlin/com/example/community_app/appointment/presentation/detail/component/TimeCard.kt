package com.example.community_app.appointment.presentation.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.appointment.domain.model.Appointment
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatIsoTime
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.point_in_time
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimeCard(
  appointment: Appointment
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
  ) {
    Column(
      modifier = Modifier.padding(Spacing.medium),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = stringResource(Res.string.point_in_time),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )
      Text(
        text = formatIsoDate(appointment.startsAt),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${formatIsoTime(appointment.startsAt)} - ${formatIsoTime(appointment.endsAt)}",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold
      )
    }
  }
}