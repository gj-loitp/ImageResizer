package ru.tech.imageresizershrinker.presentation.root.widget.modifier

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.zIndex
import com.gigamole.composeshadowsplus.common.ShadowsPlusType
import com.gigamole.composeshadowsplus.common.shadowsPlus
import ru.tech.imageresizershrinker.presentation.root.theme.outlineVariant
import ru.tech.imageresizershrinker.presentation.root.widget.utils.LocalSettingsState

fun Modifier.drawHorizontalStroke(
    top: Boolean = false,
    height: Dp = Dp.Unspecified,
    autoElevation: Dp = 6.dp,
) = composed {
    val borderWidth = LocalSettingsState.current.borderWidth
    val h = if (height.isUnspecified) {
        borderWidth.takeIf { it > 0.dp }
    } else height

    val color = MaterialTheme.colorScheme.outlineVariant(
        0.1f,
        onTopOf = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    )


    if (h == null) {
        Modifier
    } else {
        val heightPx = with(LocalDensity.current) { h.toPx() }
        zIndex(20f)
            .drawWithContent {
                drawContent()
                drawRect(
                    color = color,
                    topLeft = if (top) Offset(0f, 0f) else Offset(0f, this.size.height),
                    size = Size(this.size.width, heightPx)
                )
            }
    }
        .shadowsPlus(
            type = ShadowsPlusType.SoftLayer,
            spread = (-2).dp,
            shape = RectangleShape,
            radius = animateDpAsState(if (h == null) autoElevation else 0.dp).value
        )
        .zIndex(20f)
}