package com.example.community_app.geocoding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.community_app.geocoding.domain.Address
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.search_hint
import community_app.composeapp.generated.resources.search_no_results
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.Clock
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Search
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSearchOverlay(
  query: String,
  onQueryChange: (String) -> Unit,
  isSearching: Boolean = false,
  suggestions: List<Address>,
  onAddressClick: (Address) -> Unit,
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
          onSearch = { },
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
      LazyColumn {
        items(suggestions) { address ->
          val isHistoryItem = query.isBlank()

          ListItem(
            headlineContent = {
              Text(address.getUiLine1())
            },
            supportingContent = {
              val detail = address.getUiLine2()
              if (detail.isNotBlank()) Text(detail)
            },
            leadingContent = {
              Icon(
                imageVector = if (isHistoryItem) FeatherIcons.Clock else FeatherIcons.MapPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            modifier = Modifier
              .clickable { onAddressClick(address) }
              .fillMaxWidth()
          )
        }

        if (suggestions.isEmpty() && query.isNotBlank() && !isSearching) {
          item {
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
  }
}