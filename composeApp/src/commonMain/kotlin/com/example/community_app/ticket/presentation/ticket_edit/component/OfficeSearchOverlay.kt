package com.example.community_app.ticket.presentation.ticket_edit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.community_app.office.domain.Office
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.search_hint
import community_app.composeapp.generated.resources.search_no_results
import compose.icons.FeatherIcons
import compose.icons.feathericons.Briefcase
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.Search
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeSearchOverlay(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: (String) -> Unit,
  offices: List<Office>,
  onOfficeClick: (Office) -> Unit,
  onBackClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .zIndex(10f)
      .background(MaterialTheme.colorScheme.background)
  ) {
    SearchBar(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth(),
      inputField = {
        SearchBarDefaults.InputField(
          query = query,
          onQueryChange = onQueryChange,
          onSearch = onSearch,
          expanded = true,
          onExpandedChange = { },
          placeholder = { Text(stringResource(Res.string.search_hint)) },
          leadingIcon = {
            IconButton(onBackClick) {
              Icon(FeatherIcons.ChevronLeft, null)
            }
          },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton({ onQueryChange("") }) {
                Icon(FeatherIcons.X, null)
              }
            } else {
              Icon(FeatherIcons.Search, null)
            }
          }
        )
      },
      expanded = true,
      onExpandedChange = { }
    ) {
      Column(Modifier.verticalScroll(rememberScrollState())) {
        offices.forEach { office ->
          ListItem(
            headlineContent = { Text(office.name) },
            supportingContent = { Text("${office.address.zipCode} ${office.address.city}") },
            leadingContent = { Icon(FeatherIcons.Briefcase, null) },
            modifier = Modifier
              .clickable { onOfficeClick(office) }
              .fillMaxWidth()
          )
        }
      }
      if (offices.isEmpty() && query.isNotBlank()) {
        Text(
          text = stringResource(Res.string.search_no_results),
          modifier = Modifier.padding(16.dp),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}