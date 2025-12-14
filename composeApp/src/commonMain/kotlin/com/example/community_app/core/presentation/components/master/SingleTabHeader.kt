package com.example.community_app.core.presentation.components.master

import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SingleTabHeader(
  title: StringResource
) {
  SecondaryTabRow(
    selectedTabIndex = 0,
    containerColor = Color.Transparent,
    contentColor = MaterialTheme.colorScheme.outlineVariant,
    divider = {
      HorizontalDivider(
        modifier = Modifier.height(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant
      )
    },
    indicator = { }
  ) {
    Tab(
      selected = false,
      enabled = false,
      onClick = { },
      text = {
        Text(
          text = stringResource(title),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    )
  }
}