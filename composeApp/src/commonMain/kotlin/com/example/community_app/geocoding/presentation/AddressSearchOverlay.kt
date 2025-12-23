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
import com.example.community_app.core.presentation.components.LocationGuard
import com.example.community_app.geocoding.domain.model.Address
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.search_address_current_location
import community_app.composeapp.generated.resources.search_hint
import community_app.composeapp.generated.resources.search_no_results
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.Clock
import compose.icons.feathericons.Home
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Search
import compose.icons.feathericons.User
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSearchOverlay(
  query: String,
  onQueryChange: (String) -> Unit,
  isSearching: Boolean = false,
  isLocationAvailable: Boolean = false,
  suggestions: List<AddressSuggestion>,
  onAddressClick: (Address) -> Unit,
  onUseCurrentLocationClick: () -> Unit,
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
        item {
          LocationGuard {
            ListItem(
              headlineContent = {
                Text(stringResource(Res.string.search_address_current_location))
              },
              leadingContent = {
                Icon(
                  imageVector = FeatherIcons.User,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              modifier = Modifier
                .clickable { onUseCurrentLocationClick() }
                .fillMaxWidth()
            )
          }
        }

        items(suggestions) { suggestion ->
          val address = suggestion.address

          ListItem(
            headlineContent = {
              Text(address.getUiLine1())
            },
            supportingContent = {
              val detail = address.getUiLine2()
              if (detail.isNotBlank()) Text(detail)
            },
            leadingContent = {
              val icon = when (suggestion.type) {
                AddressSuggestionType.HISTORY -> FeatherIcons.Clock
                AddressSuggestionType.API -> FeatherIcons.MapPin
                AddressSuggestionType.HOME -> FeatherIcons.Home
              }

              Icon(
                imageVector = icon,
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