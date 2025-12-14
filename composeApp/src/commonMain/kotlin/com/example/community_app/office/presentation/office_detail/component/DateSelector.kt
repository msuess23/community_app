package com.example.community_app.office.presentation.office_detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.formatMillisDate
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.Calendar

@Composable
fun DateSelector(
  dateMillis: Long,
  dateRange: LongRange,
  onPrev: () -> Unit,
  onNext: () -> Unit,
  onCalendarClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = Spacing.medium, vertical = Spacing.small),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onPrev) {
      Icon(
        imageVector = FeatherIcons.ArrowLeft,
        contentDescription = null,
        tint = if (dateMillis > dateRange.first) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable(onClick = onCalendarClick)
        .padding(Spacing.small)
    ) {
      Icon(
        imageVector = FeatherIcons.Calendar,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.width(Spacing.small))
      Text(
        text = formatMillisDate(dateMillis),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    IconButton(onClick = onNext) {
      Icon(
        imageVector = FeatherIcons.ArrowRight,
        contentDescription = null,
        tint = if (addDays(dateMillis, 1)  < dateRange.last) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
      )
    }
  }
}