package com.example.community_app.core.presentation.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.clear_hint
import community_app.composeapp.generated.resources.search_hint
import compose.icons.FeatherIcons
import compose.icons.feathericons.Search
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onImeSearch: () -> Unit,
  modifier: Modifier = Modifier
) {
  CompositionLocalProvider(
    LocalTextSelectionColors provides TextSelectionColors(
      handleColor = Color.Yellow,
      backgroundColor = Color.Yellow
    )
  ) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      shape = RoundedCornerShape(100),
      colors = OutlinedTextFieldDefaults.colors(
        cursorColor = Color.Blue,
        focusedBorderColor = Color.Yellow
      ),
      placeholder = {
        Text(text = stringResource(Res.string.search_hint))
      },
      leadingIcon = {
        Icon(
          imageVector = FeatherIcons.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f)
        )
      },
      singleLine = true,
      keyboardActions = KeyboardActions(
        onSearch = {
          onImeSearch()
        }
      ),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search
      ),
      trailingIcon = {
        AnimatedVisibility(
          visible = searchQuery.isNotBlank()
        ) {
          IconButton(
            onClick = {
              onSearchQueryChange("")
            }
          ) {
            Icon(
              imageVector = FeatherIcons.X,
              contentDescription = stringResource(Res.string.clear_hint),
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      },
      modifier = modifier
        .background(
          shape = RoundedCornerShape(100),
          color = Color.White
        )
        .minimumInteractiveComponentSize()
    )
  }
}