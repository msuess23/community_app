package com.example.community_app.office.presentation.office_detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.theme.Spacing
import compose.icons.FeatherIcons
import compose.icons.feathericons.Mail
import compose.icons.feathericons.Phone

@Composable
fun ContactCard(
  phoneNumber: String?,
  email: String?
) {
  val uriHandler = LocalUriHandler.current

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(Spacing.medium)) {
      if (!phoneNumber.isNullOrBlank()) {
        Row(
          modifier = Modifier
            .padding(Spacing.extraSmall)
            .clickable {
              val sanitizedNumber = phoneNumber
                .replace(" ", "")
                .replace("-", "")
              uriHandler.openUri("tel:$sanitizedNumber")
            },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = FeatherIcons.Phone,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )

          Spacer(Modifier.width(8.dp))

          Text(
            text = phoneNumber,
            style = MaterialTheme.typography.bodyLarge
          )
        }
      }

      if (!email.isNullOrBlank()) {
        Row(
          modifier = Modifier
            .padding(Spacing.extraSmall)
            .clickable { uriHandler.openUri("mailto:${email}") },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = FeatherIcons.Mail,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )

          Spacer(Modifier.width(8.dp))

          Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge
          )
        }
      }
    }
  }
}