/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.core.ui.widget.sliders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.helper.ProvidesValue
import ru.tech.imageresizershrinker.core.ui.utils.helper.rememberRipple
import ru.tech.imageresizershrinker.core.ui.utils.provider.SafeLocalContainerColor
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.materialShadow
import ru.tech.imageresizershrinker.core.ui.widget.modifier.trackOverslide

@Composable
fun FancySlider(
    value: Float,
    enabled: Boolean,
    colors: SliderColors,
    interactionSource: MutableInteractionSource,
    thumbShape: Shape,
    modifier: Modifier,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    val thumbColor by animateColorAsState(
        if (enabled) colors.thumbColor else colors.disabledThumbColor
    )

    val animatedValue by animateFloatAsState(
        targetValue = value,
        spring(stiffness = 15_000f, dampingRatio = 0.2f),
        label = "animatedValue",
    )
    val thumb: @Composable (SliderState) -> Unit = { sliderState ->
        val sliderFraction by remember {
            derivedStateOf {
                (animatedValue - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
            }
        }
        Spacer(
            Modifier
                .zIndex(100f)
                .rotate(1080f * sliderFraction)
                .size(26.dp)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(
                        bounded = false,
                        radius = 24.dp
                    )
                )
                .hoverable(interactionSource = interactionSource)
                .materialShadow(
                    shape = thumbShape,
                    elevation = 1.dp,
                    enabled = LocalSettingsState.current.drawSliderShadows
                )
                .background(thumbColor, thumbShape)
        )
    }

    val settingsState = LocalSettingsState.current
    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        var scaleX by remember { mutableFloatStateOf(1f) }
        var scaleY by remember { mutableFloatStateOf(1f) }
        var translateX by remember { mutableFloatStateOf(0f) }
        var transformOrigin by remember { mutableStateOf(TransformOrigin.Center) }
        CustomSlider(
            interactionSource = interactionSource,
            thumb = thumb,
            enabled = enabled,
            modifier = modifier
                .graphicsLayer {
                    this.transformOrigin = transformOrigin
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    this.translationX = translateX
                }
                .container(
                    shape = CircleShape,
                    autoShadowElevation = animateDpAsState(
                        if (settingsState.drawSliderShadows) {
                            1.dp
                        } else 0.dp
                    ).value,
                    resultPadding = 0.dp,
                    borderColor = MaterialTheme.colorScheme
                        .outlineVariant(
                            luminance = 0.1f,
                            onTopOf = SwitchDefaults.colors().disabledCheckedTrackColor
                        )
                        .copy(0.3f),
                    color = SafeLocalContainerColor
                        .copy(0.5f)
                        .compositeOver(MaterialTheme.colorScheme.surface)
                        .copy(colors.activeTrackColor.alpha),
                    composeColorOnTopOfBackground = false
                )
                .padding(horizontal = 6.dp),
            colors = colors.toCustom(),
            value = animateFloatAsState(
                targetValue = value,
                animationSpec = tween(200)
            ).value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            track = { sliderState ->
                val sliderFraction by remember {
                    derivedStateOf {
                        (animatedValue - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                    }
                }

                val density = LocalDensity.current
                val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

                CustomSliderDefaults.Track(
                    sliderState = sliderState,
                    colors = colors.toCustom(),
                    trackHeight = 38.dp,
                    modifier = Modifier.trackOverslide(value = sliderFraction) { overslide ->
                        transformOrigin = TransformOrigin(
                            pivotFractionX = when (isLtr) {
                                true -> if (sliderFraction < .5f) 2f else -1f
                                false -> if (sliderFraction < .5f) -1f else 2f
                            },
                            pivotFractionY = .5f,
                        )

                        when (sliderFraction) {
                            in 0f..(.5f) -> {
                                scaleY = 1f + overslide * 2f
                                scaleX = 1f - (overslide * .0025f)
                            }

                            else -> {
                                scaleY = 1f - overslide * 2f
                                scaleX = 1f + (overslide * .0025f)
                            }
                        }

                        translateX = overslide * with(density) { 24.dp.toPx() }
                    }
                )
            }
        )
    }
}

@Composable
internal fun SliderColors.toCustom(): CustomSliderColors = remember(this) {
    derivedStateOf {
        CustomSliderColors(
            thumbColor = thumbColor,
            activeTrackColor = activeTrackColor,
            activeTickColor = activeTickColor,
            inactiveTrackColor = inactiveTrackColor,
            inactiveTickColor = inactiveTickColor,
            disabledThumbColor = disabledThumbColor,
            disabledActiveTrackColor = disabledActiveTrackColor,
            disabledActiveTickColor = disabledActiveTickColor,
            disabledInactiveTrackColor = disabledInactiveTrackColor,
            disabledInactiveTickColor = disabledInactiveTickColor
        )
    }
}.value