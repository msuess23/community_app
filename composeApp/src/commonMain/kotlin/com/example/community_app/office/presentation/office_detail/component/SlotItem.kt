package com.example.community_app.office.presentation.office_detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatMillisTime
import com.example.community_app.core.util.parseIsoToMillis
import compose.icons.FeatherIcons
import compose.icons.feathericons.Clock

@Composable
fun SlotItem(slot: Slot, onClick: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = Spacing.medium, vertical = 4.dp)
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = FeatherIcons.Clock,
        contentDescription = null,
        modifier = Modifier.width(16.dp),
        tint = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.width(8.dp))

      val startMillis = parseIsoToMillis(slot.startIso)
      val endMillis = parseIsoToMillis(slot.endIso)
      val startStr = formatMillisTime(startMillis)
      val endStr = formatMillisTime(endMillis)

      Text(
        text = "$startStr - $endStr",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium
      )
    }
  }
}