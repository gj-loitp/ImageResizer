package ru.tech.imageresizershrinker.presentation.root.widget.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.presentation.filters_screen.components.ValueDialog
import ru.tech.imageresizershrinker.presentation.filters_screen.components.ValueText
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container

@Composable
fun EnhancedSliderItem(
    value: Number,
    title: String,
    modifier: Modifier = Modifier,
    sliderModifier: Modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
    icon: ImageVector? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: ((Float) -> Unit)? = null,
    steps: Int = 0,
    topContentPadding: Dp = 8.dp,
    valuePrefix: String = "",
    visible: Boolean = true,
    color: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(16.dp),
    additionalContent: (@Composable () -> Unit)? = null
) {
    var showValueDialog by rememberSaveable { mutableStateOf(false) }
    var internalState by remember(value) { mutableStateOf(value) }
    AnimatedVisibility(visible = visible) {
        Column(
            modifier = modifier
                .container(
                    shape = shape,
                    color = color
                )
                .animateContentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(
                                top = topContentPadding,
                                start = 16.dp
                            )
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(
                            top = topContentPadding,
                            end = 16.dp,
                            start = 16.dp
                        )
                        .weight(1f),
                    lineHeight = 18.sp
                )
                ValueText(
                    value = value,
                    valuePrefix = valuePrefix,
                    modifier = Modifier.padding(
                        top = topContentPadding,
                        end = 8.dp
                    ),
                    onClick = {
                        showValueDialog = true
                    }
                )
            }
            EnhancedSlider(
                modifier = sliderModifier,
                value = animateFloatAsState(internalState.toFloat()).value,
                onValueChange = {
                    internalState = it
                    onValueChange(it)
                },
                onValueChangeFinished = onValueChangeFinished?.let {
                    {
                        it(internalState.toFloat())
                    }
                },
                valueRange = valueRange,
                steps = steps
            )
            additionalContent?.invoke()
        }
    }
    ValueDialog(
        roundTo = null,
        valueRange = valueRange,
        valueState = value.toString(),
        expanded = visible && showValueDialog,
        onDismiss = { showValueDialog = false },
        onValueUpdate = {
            onValueChange(it)
            onValueChangeFinished?.invoke(it)
        }
    )
}