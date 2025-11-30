package com.example.community_app.core.presentation.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.theme.Size
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.search_clear
import community_app.composeapp.generated.resources.search_hint
import compose.icons.FeatherIcons
import compose.icons.feathericons.Search
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onImeSearch: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }

  val colors = OutlinedTextFieldDefaults.colors(
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface
  )

  CompositionLocalProvider(
    LocalTextSelectionColors provides TextSelectionColors(
      handleColor = MaterialTheme.colorScheme.primary,
      backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    )
  ) {
    BasicTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier = modifier
        .fillMaxWidth()
        .minimumInteractiveComponentSize(),
      textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = colors.focusedTextColor
      ),
      cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search
      ),
      keyboardActions = KeyboardActions(
        onSearch = { onImeSearch() }
      ),
      singleLine = true,
      interactionSource = interactionSource,
      decorationBox = { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
          value = searchQuery,
          innerTextField = innerTextField,
          enabled = true,
          singleLine = true,
          visualTransformation = VisualTransformation.None,
          interactionSource = interactionSource,
          isError = false,
          label = null,
          placeholder = {
            Text(
              text = stringResource(Res.string.search_hint),
              style = MaterialTheme.typography.bodyLarge
            )
          },
          leadingIcon = {
            Icon(
              imageVector = FeatherIcons.Search,
              contentDescription = null,
              modifier = Modifier.size(Size.iconMedium)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(
                  imageVector = FeatherIcons.X,
                  contentDescription = stringResource(Res.string.search_clear),
                  modifier = Modifier.size(Size.iconMedium)
                )
              }
            }
          },
          colors = colors,
          contentPadding = PaddingValues(
            horizontal = Spacing.medium,
            vertical = 0.dp
          ),
          container = {
            OutlinedTextFieldDefaults.Container(
              enabled = true,
              isError = false,
              interactionSource = interactionSource,
              colors = colors,
              shape = RoundedCornerShape(100)
            )
          }
        )
      }
    )
  }
}