package ru.tech.imageresizershrinker.presentation.generate_palette_screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedSliderItem
import kotlin.math.roundToInt

@Composable
fun PaletteColorsCountSelector(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    EnhancedSliderItem(
        modifier = modifier.padding(horizontal = 16.dp),
        value = value,
        icon = Icons.Rounded.Palette,
        title = stringResource(R.string.max_colors_count),
        onValueChange = {},
        onValueChangeFinished = {
            onValueChange(it.roundToInt())
        },
        valueRange = 1f..128f,
        steps = 127,
        shape = RoundedCornerShape(24.dp)
    )
}