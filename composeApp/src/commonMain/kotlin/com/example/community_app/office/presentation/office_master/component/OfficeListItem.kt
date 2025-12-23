package com.example.community_app.office.presentation.office_master.component

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.office.domain.model.Office
import compose.icons.FeatherIcons
import compose.icons.feathericons.Briefcase
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Clock
import compose.icons.feathericons.MapPin

@Composable
fun OfficeListItem(
  office: Office,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(Spacing.medium),
    modifier = modifier
      .height(100.dp)
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surfaceContainer
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(Spacing.medium),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(Spacing.small),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(48.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = FeatherIcons.Briefcase,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.width(Spacing.medium))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = office.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = FeatherIcons.MapPin,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.secondary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${office.address.street} ${office.address.houseNumber}, ${office.address.city}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (!office.openingHours.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = FeatherIcons.Clock,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = office.openingHours,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.tertiary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
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