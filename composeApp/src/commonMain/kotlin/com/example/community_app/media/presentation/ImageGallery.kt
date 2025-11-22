package com.example.community_app.media.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.image_placeholder
import org.jetbrains.compose.resources.painterResource

@Composable
fun ImageGallery(
  imageUrls: List<String>,
  modifier: Modifier = Modifier
) {
  if (imageUrls.isEmpty()) {
    AsyncImage(
      model = null,
      contentDescription = null,
      placeholder = painterResource(Res.drawable.image_placeholder),
      contentScale = ContentScale.Crop,
      modifier = modifier
    )
  } else if (imageUrls.size == 1) {
    AsyncImage(
      model = imageUrls.first(),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = modifier
    )
  } else {
    Box(modifier = modifier) {
      val pagerState = rememberPagerState(pageCount = { imageUrls.size })

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { page ->
        AsyncImage(
          model = imageUrls[page],
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      Row(
        Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        repeat(pagerState.pageCount) { iteration ->
          val color = if (pagerState.currentPage == iteration)
            MaterialTheme.colorScheme.primary
          else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(color)
              .size(8.dp)
          )
        }
      }
    }
  }
}