package com.example.community_app.core.presentation.components.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.community_app.core.domain.model.Address
import compose.icons.FeatherIcons
import compose.icons.feathericons.MapPin

@Composable
fun CommunityAddressCard(
  address: Address?,
  modifier: Modifier = Modifier
) {
  if (address == null) return

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = FeatherIcons.MapPin,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 2.dp)
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column {
        Text(
          text = "${address.street ?: ""} ${address.houseNumber ?: ""}".trim(),
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "${address.zipCode ?: ""} ${address.city ?: ""}".trim(),
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}