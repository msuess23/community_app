package com.example.community_app.appointment.presentation.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.office.domain.model.Office
import compose.icons.FeatherIcons
import compose.icons.feathericons.Mail
import compose.icons.feathericons.Phone

@Composable
fun AppointmentOfficeCard(
  office: Office
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = office.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )

      if (!office.phone.isNullOrBlank() || !office.contactEmail.isNullOrBlank()) {
        Column {
          if (!office.phone.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = FeatherIcons.Phone,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )

              Spacer(Modifier.width(8.dp))

              Text(
                text = office.phone,
                style = MaterialTheme.typography.bodyMedium
              )
            }
          }
          if (!office.contactEmail.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = FeatherIcons.Mail,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )

              Spacer(Modifier.width(8.dp))

              Text(
                text = office.contactEmail,
                style = MaterialTheme.typography.bodyMedium
              )
            }
          }
        }
      }
    }
  }
}