package com.example.community_app.core.presentation.components.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.example.community_app.core.presentation.theme.Size
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.draft_label
import community_app.composeapp.generated.resources.image_placeholder
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Star
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoTicketListItem(
  title: String,
  subtitle: String?,
  dateString: String,
  imageUrl: String?,
  isDraft: Boolean = false,
  isFavorite: Boolean = false,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
//  val startDate = formatIsoDate(info.startsAt)
//  val endDate = formatIsoDate(info.endsAt)
//  val dateString = if (startDate == endDate) startDate else "$startDate - $endDate"
//
//  val categoryText = info.category.toUiText().asString()
//  val statusText = info.currentStatus?.toUiText()?.asString()
//  val sublineText = if (statusText != null) "$categoryText, $statusText" else categoryText

  Surface(
    shape = RoundedCornerShape(Spacing.medium),
    modifier = modifier
      .height(120.dp)
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surfaceContainer
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .weight(0.3f)
          .fillMaxHeight(),
        contentAlignment = Alignment.Center
      ) {
        if (imageUrl != null) {
          var imageLoadResult by remember {
            mutableStateOf<Result<Painter>?>(null)
          }
          val painter = rememberAsyncImagePainter(
            model = imageUrl,
            onSuccess = { imageLoadResult = Result.success(it.painter) },
            onError = {
              it.result.throwable.printStackTrace()
              imageLoadResult = Result.failure(it.result.throwable)
            }
          )

          when(val result = imageLoadResult) {
            null -> CircularProgressIndicator(
              modifier = Modifier.size(Size.iconMedium),
              color = MaterialTheme.colorScheme.primary
            )
            else -> {
              ThumbnailImage(
                painter = painter,
                isSuccess = result.isSuccess,
                contentDescription = title
              )
            }
          }
        } else {
          ThumbnailImage(
            painter = null,
            isSuccess = false
          )
        }
      }

      Column(
        modifier = Modifier
          .weight(0.7f)
          .fillMaxHeight()
          .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.Center,
      ) {
        if (isDraft) {
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text(
              text = stringResource(Res.string.draft_label),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
          }
        }

        if (isFavorite) {
          Icon(
            imageVector = FeatherIcons.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
          )
        }

        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = dateString,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.secondary
        )
      }

      Icon(
        imageVector = FeatherIcons.ChevronRight,
        contentDescription = null,
        modifier = Modifier
          .padding(end = Spacing.medium)
          .size(Size.iconLarge),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}


@Composable
private fun ThumbnailImage(
  painter: AsyncImagePainter?,
  isSuccess: Boolean,
  contentDescription: String = "placeholder image"
) {
  Image(
    painter = if(isSuccess && painter != null) painter else {
      painterResource(Res.drawable.image_placeholder)
    },
    contentDescription = contentDescription,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
  )
}