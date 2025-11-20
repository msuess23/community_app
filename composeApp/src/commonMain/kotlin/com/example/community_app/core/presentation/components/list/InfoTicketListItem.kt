package com.example.community_app.core.presentation.components.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.community_app.info.domain.Info
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.image_placeholder
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import org.jetbrains.compose.resources.painterResource

@Composable
fun InfoTicketListItem(
  info: Info,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(32.dp),
    modifier = modifier
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surfaceContainerLow
  ) {
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .height(IntrinsicSize.Min),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .height(100.dp),
        contentAlignment = Alignment.Center
      ) {
        var imageLoadResult by remember {
          mutableStateOf<Result<Painter>?>(null)
        }
        val painter = rememberAsyncImagePainter(
          model = info.imageUrl,
          onSuccess = {
            imageLoadResult = if(it.painter.intrinsicSize.width > 1 && it.painter.intrinsicSize.height > 1) {
              Result.success(it.painter)
            } else {
              Result.failure(Exception("Invalid image size"))
            }
          },
          onError = {
            it.result.throwable.printStackTrace()
            imageLoadResult = Result.failure(it.result.throwable)
          }
        )

        when(val result = imageLoadResult) {
          null -> CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
          )
          else -> {
            Image(
              painter = if(result.isSuccess) painter else {
                painterResource(Res.drawable.image_placeholder)
              },
              contentDescription = info.title,
              contentScale = if(result.isSuccess) {
                ContentScale.Crop
              } else {
                ContentScale.Fit
              },
              modifier = Modifier
                .aspectRatio(
                  ratio = 0.65f,
                  matchHeightConstraintsFirst = true
                )
            )
          }
        }
      }

      Column(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = info.title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        // TODO: more info attributes as text
      }

      Icon(
        imageVector = FeatherIcons.ChevronRight,
        contentDescription = null,
        modifier = Modifier
          .size(36.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}