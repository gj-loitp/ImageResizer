package ru.tech.imageresizershrinker.main_screen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.twotone.Image
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.theme.outlineVariant
import ru.tech.imageresizershrinker.utils.modifier.block
import ru.tech.imageresizershrinker.utils.modifier.fabBorder
import ru.tech.imageresizershrinker.widget.utils.LocalSettingsState

@Composable
fun FabPreview(
    modifier: Modifier = Modifier,
    alignment: Alignment,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / 1.7f)
            .padding(4.dp)
            .fabBorder(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val settingsState = LocalSettingsState.current
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fabBorder(shape = shapes.small, elevation = 4.dp)
                .block(shape = shapes.small)
                .fillMaxWidth(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            FilledIconButton(
                onClick = {},
                modifier = Modifier.size(25.dp),
                shape = RoundedCornerShape(4.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colorScheme.surfaceColorAtElevation(6.dp),
                    contentColor = colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            settingsState.borderWidth,
                            colorScheme.outlineVariant(0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(3.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.pick_image),
                modifier = Modifier.padding(4.dp),
                fontSize = 3.sp,
                lineHeight = 4.sp,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant
            )
        }
        val weight by animateFloatAsState(
            targetValue = when (alignment) {
                Alignment.BottomStart -> 0f
                Alignment.BottomCenter -> 0.5f
                else -> 1f
            }
        )

        CompositionLocalProvider(LocalContentColor provides colorScheme.onPrimaryContainer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(0.01f + weight))
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(22.dp)
                        .fabBorder(shape = RoundedCornerShape(7.dp), elevation = 4.dp)
                        .background(
                            color = colorScheme.primaryContainer,
                            shape = RoundedCornerShape(7.dp),
                        )
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, null, Modifier.size(10.dp))
                }
                Spacer(modifier = Modifier.weight(1.01f - weight))
            }
        }
    }
}