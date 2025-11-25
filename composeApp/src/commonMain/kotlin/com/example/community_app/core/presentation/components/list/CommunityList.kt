package com.example.community_app.core.presentation.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.community_app.info.domain.Info

@Composable
fun CustomList(
  infos: List<Info>,
  onInfoClick: (Info) -> Unit,
  modifier: Modifier = Modifier,
  scrollState: LazyListState = rememberLazyListState()
) {
  LazyColumn(
    modifier = modifier,
    state = scrollState,
    verticalArrangement = Arrangement.spacedBy(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    items(
      items = infos,
      key = { it.id }
    ) { info ->
      InfoTicketListItem(
        info = info,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        onClick = {
          onInfoClick(info)
        }
      )
    }
  }
}
