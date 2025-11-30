package com.example.community_app.core.presentation.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.Thumb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  valueRange: ClosedFloatingPointRange<Float>,
  modifier: Modifier = Modifier,
  steps: Int = 0,
  enabled: Boolean = true,
  updateContinuously: Boolean = true,
  onInteractionFinished: (() -> Unit)? = null,
  valueLabel: @Composable ((displayValue: Float) -> Unit)? = null,
  helperLabel: @Composable (() -> Unit)? = null,
  colors: SliderColors = SliderDefaults.sliderColors(),
  roundToInt: Boolean = false
) {
  var sliderPosition by remember { mutableFloatStateOf(value) }
  val interactionSource = remember { MutableInteractionSource() }

  val sliderFocusRequester = remember { FocusRequester() }

  LaunchedEffect(value) {
    sliderPosition = value
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    valueLabel?.invoke(sliderPosition)

    Slider(
      modifier =
        Modifier
          // Makes the slider focusable and allows focus to be requested
          .focusRequester(sliderFocusRequester)
          .focusable()
          // eavesdrop pointer events, but don't consume it — just request the focus
          .pointerInput(Unit) {
            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent()
                // Request focus on slider when user starts interacting with it
                if (event.changes.any { it.pressed }) {
                  sliderFocusRequester.requestFocus()
                }
              }
            }
          },
      value = sliderPosition,
      onValueChange = { newValue ->
        sliderPosition =
          if (roundToInt) {
            newValue.toInt().toFloat()
          } else {
            newValue
          }

        if (updateContinuously) {
          onValueChange(newValue)
        }
      },
      enabled = enabled,
      valueRange = valueRange,
      steps = steps,
      onValueChangeFinished = {
        if (!updateContinuously) {
          if (roundToInt) {
            sliderPosition = sliderPosition.toInt().toFloat()
          }
          onValueChange(sliderPosition)
        }

        onInteractionFinished?.invoke()
      },
      track = { sliderState ->
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(8.dp))
        ) {
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(colors.inactiveTrackColor)
          )
          Box(
            modifier =
              Modifier
                .fillMaxWidth(sliderState.value / valueRange.endInclusive)
                .height(4.dp)
                .background(colors.activeTrackColor)
          )
        }
      },
      thumb = {
        Thumb(
          modifier =
            Modifier
              .shadow(elevation = 5.dp, shape = CircleShape, clip = false)
              .size(24.dp),
          interactionSource = interactionSource,
          colors = SliderDefaults.colors(thumbColor = colors.thumbColor)
        )
      },
      interactionSource = interactionSource
    )

    helperLabel?.invoke()
  }
}

@Composable
fun SliderDefaults.sliderColors(): SliderColors {
  return colors(
    activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
    thumbColor = MaterialTheme.colorScheme.primary
  )
}