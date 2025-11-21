package com.example.community_app.info.presentation.info_master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.util.InfoCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoFilterSheet(
  filterState: InfoFilterState,
  onAction: (InfoMasterAction) -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = {
      onAction(InfoMasterAction.OnToggleFilterSheet)
    },
    sheetState = sheetState,
    contentColor = MaterialTheme.colorScheme.surfaceContainer,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .padding(bottom = 48.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Filter & Sortierung",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
        TextButton(
          onClick = {
            onAction(InfoMasterAction.OnClearCategories)
            onAction(InfoMasterAction.OnSortChange(InfoSortOption.DATE_DESC))
          }
        ) {
          Text("Zurücksetzen")
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

      Text(
        text = "Sortieren nach",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      Column(modifier = Modifier.selectableGroup()) {
        SortOptionRow(
          text = "Neueste zuerst",
          selected = filterState.sortBy == InfoSortOption.DATE_DESC,
          onClick = { onAction(InfoMasterAction.OnSortChange(InfoSortOption.DATE_DESC)) }
        )
        SortOptionRow(
          text = "Älteste zuerst",
          selected = filterState.sortBy == InfoSortOption.DATE_ASC,
          onClick = { onAction(InfoMasterAction.OnSortChange(InfoSortOption.DATE_ASC)) }
        )
        SortOptionRow(
          text = "Alphabetisch (A-Z)",
          selected = filterState.sortBy == InfoSortOption.ALPHABETICAL,
          onClick = { onAction(InfoMasterAction.OnSortChange(InfoSortOption.ALPHABETICAL)) }
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

      Text(
        text = "Kategorien",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val isAllSelected = filterState.selectedCategories.isEmpty()
        FilterChip(
          selected = isAllSelected,
          onClick = { onAction(InfoMasterAction.OnClearCategories) },
          label = { Text("Alle") }
        )

        InfoCategory.entries.forEach { category ->
          val isSelected = category in filterState.selectedCategories
          FilterChip(
            selected = isSelected,
            onClick = { onAction(InfoMasterAction.OnCategorySelect(category)) },
            label = {
              // Hier könnten wir später Strings aus resources laden
              // Vorerst einfache Capitalization:
              Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
            }
          )
        }
      }
    }
  }
}

@Composable
private fun SortOptionRow(
  text: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  Row(
    Modifier
      .fillMaxWidth()
      .height(48.dp)
      .selectable(
        selected = selected,
        onClick = onClick,
        role = Role.RadioButton
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = selected,
      onClick = null
    )
    Spacer(Modifier.padding(start = 16.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}