package com.example.community_app.ticket.presentation.ticket_edit.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.community_app.ticket.presentation.ticket_edit.TicketImageState
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.images_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageSelectionSection(
  coverImageUri: String? = null,
  images: List<TicketImageState>,
  onAddImage: () -> Unit,
  onImageClick: (TicketImageState) -> Unit,
  onRemoveImage: (TicketImageState) -> Unit
) {
  Text(
    text = stringResource(Res.string.images_label),
    style = MaterialTheme.typography.titleMedium
  )
  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth().height(100.dp)
  ) {
    // Add Button
    item {
      Box(
        modifier = Modifier
          .size(100.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
          .clickable { onAddImage() },
        contentAlignment = Alignment.Center
      ) {
        Icon(FeatherIcons.Plus, null)
      }
    }

    // Image List
    items(images) { image ->
      val isCover = image.uri == coverImageUri
      Box(
        modifier = Modifier
          .size(100.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(
            width = if (isCover) 3.dp else 0.dp,
            color = if (isCover) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(8.dp)
          )
          .clickable { onImageClick(image) }
      ) {
        Image(
          painter = rememberAsyncImagePainter(image.uri),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Delete Button Overlay
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .size(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .clickable { onRemoveImage(image) },
          contentAlignment = Alignment.Center
        ) {
          Icon(FeatherIcons.X, null, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}