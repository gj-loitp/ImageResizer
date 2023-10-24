package ru.tech.imageresizershrinker.presentation.draw_screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.util.ColorUtil.roundToTwoDigits
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedSliderItem

@Composable
fun LineWidthSelector(
    modifier: Modifier,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    EnhancedSliderItem(
        modifier = modifier,
        value = value,
        icon = Icons.Rounded.LineWeight,
        title = stringResource(R.string.line_width),
        valuePrefix = " Pt",
        sliderModifier = Modifier
            .padding(top = 14.dp, start = 12.dp, end = 12.dp, bottom = 10.dp),
        valueRange = 1f..100f,
        onValueChange = {
            onValueChange(it.roundToTwoDigits())
        },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    )
}