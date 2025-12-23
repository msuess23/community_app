package com.example.community_app.office.presentation.office_detail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.components.detail.CommunityAddressCard
import com.example.community_app.core.presentation.components.detail.CommunityExpandableDescription
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.office.domain.model.Office
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.office_hours
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp
import compose.icons.feathericons.Clock
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfficeHeader(
  office: Office,
  isDescriptionExpanded: Boolean,
  onToggle: () -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    modifier = Modifier.padding(Spacing.medium)
  ) {
    if (!office.description.isNullOrBlank()) {
      CommunityExpandableDescription(
        text = office.description,
        isExpanded = isDescriptionExpanded,
        onToggle = onToggle
      )
    }

    if (!office.services.isNullOrBlank()) {
      Text(
        text = office.services,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    if (!office.openingHours.isNullOrBlank()) {
      Surface(
        shape = RoundedCornerShape(Spacing.medium),
        color = MaterialTheme.colorScheme.surfaceContainer
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = FeatherIcons.Clock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
          )
          Spacer(Modifier.width(16.dp))
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = stringResource(Res.string.office_hours),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = office.openingHours,
              style = MaterialTheme.typography.bodyLarge
            )
          }
        }
      }
    }

    // Contact Box
    if (!office.phone.isNullOrBlank() || !office.contactEmail.isNullOrBlank()) {
      ContactCard(
        phoneNumber = office.phone,
        email = office.contactEmail
      )
    }

    // Address
    CommunityAddressCard(address = office.address)
  }
}