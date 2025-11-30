package com.example.community_app.office.presentation.office_detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatMillisDate
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ArrowRight

@Composable
fun DateSelector(
  dateMillis: Long,
  onPrev: () -> Unit,
  onNext: () -> Unit
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
        tint = MaterialTheme.colorScheme.primary
      )
    }

    Text(
      text = formatMillisDate(dateMillis),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    IconButton(onClick = onNext) {
      Icon(
        imageVector = FeatherIcons.ArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    }
  }
}