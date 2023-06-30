package ru.tech.imageresizershrinker.widget.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.theme.mixedColor
import ru.tech.imageresizershrinker.theme.onMixedColor
import ru.tech.imageresizershrinker.utils.modifier.block
import ru.tech.imageresizershrinker.widget.buttons.GroupRipple
import ru.tech.imageresizershrinker.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.widget.utils.LocalSettingsState

@Composable
fun ExtensionGroup(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    mimeTypeInt: Int,
    orientation: Orientation = Orientation.Horizontal,
    onMimeChange: (Int) -> Unit
) {
    val settingsState = LocalSettingsState.current
    val cornerRadius = 20.dp

    val disColor = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.38f)
        .compositeOver(MaterialTheme.colorScheme.surface)

    ProvideTextStyle(
        value = TextStyle(
            color = if (!enabled) disColor
            else Color.Unspecified
        )
    ) {
        if (orientation == Orientation.Horizontal) {
            Column(
                modifier = modifier
                    .block(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    )
                    .offset(x = 0.dp, y = 9.dp)
                    .padding(start = 4.dp, end = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.extension),
                    Modifier
                        .fillMaxWidth()
                        .offset(x = 0.dp, y = (-1).dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                CompositionLocalProvider(
                    LocalRippleTheme provides GroupRipple
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("JPG", "WEBP").forEachIndexed { index, item ->
                                OutlinedButton(
                                    enabled = enabled,
                                    onClick = { onMimeChange(index) },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .widthIn(min = 48.dp)
                                        .weight(1f)
                                        .then(
                                            when (index) {
                                                0 ->
                                                    Modifier
                                                        .offset(0.dp, 0.dp)
                                                        .zIndex(if (mimeTypeInt == 0) 1f else 0f)

                                                else ->
                                                    Modifier
                                                        .offset((-1 * index).dp, 0.dp)
                                                        .zIndex(if (mimeTypeInt == index) 1f else 0f)
                                            }
                                        ),
                                    shape = when (index) {
                                        0 -> RoundedCornerShape(
                                            topStart = cornerRadius,
                                            topEnd = 0.dp,
                                            bottomStart = 0.dp,
                                            bottomEnd = 0.dp
                                        )

                                        else -> RoundedCornerShape(
                                            topStart = 0.dp,
                                            topEnd = cornerRadius,
                                            bottomStart = 0.dp,
                                            bottomEnd = 0.dp
                                        )
                                    },
                                    border = BorderStroke(
                                        max(settingsState.borderWidth, 1.dp),
                                        MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (!enabled) disColor
                                        else if (mimeTypeInt == index) MaterialTheme.colorScheme.mixedColor
                                        else Color.Transparent
                                    )
                                ) {
                                    AutoSizeText(
                                        text = item,
                                        color = if (!enabled) disColor
                                        else if (mimeTypeInt == index) MaterialTheme.colorScheme.onMixedColor
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("JPEG", "PNG").forEachIndexed { i, item ->
                                val index = i + 2
                                OutlinedButton(
                                    enabled = enabled,
                                    onClick = { onMimeChange(index) },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier
                                        .widthIn(min = 48.dp)
                                        .weight(1f)
                                        .then(
                                            when (index) {
                                                2 ->
                                                    Modifier
                                                        .offset(0.dp, (-9).dp)
                                                        .zIndex(if (mimeTypeInt == 0) 2f else 1f)

                                                else ->
                                                    Modifier
                                                        .offset((-1 * i).dp, (-9).dp)
                                                        .zIndex(if (mimeTypeInt == index) 2f else 1f)
                                            }
                                        ),
                                    shape = when (index) {
                                        2 -> RoundedCornerShape(
                                            topStart = 0.dp,
                                            topEnd = 0.dp,
                                            bottomStart = cornerRadius,
                                            bottomEnd = 0.dp
                                        )

                                        else -> RoundedCornerShape(
                                            topStart = 0.dp,
                                            topEnd = 0.dp,
                                            bottomStart = 0.dp,
                                            bottomEnd = cornerRadius
                                        )
                                    },
                                    border = BorderStroke(
                                        max(settingsState.borderWidth, 1.dp),
                                        MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (!enabled) disColor
                                        else if (mimeTypeInt == index) MaterialTheme.colorScheme.mixedColor
                                        else Color.Transparent
                                    )
                                ) {
                                    AutoSizeText(
                                        text = item,
                                        color = if (!enabled) disColor
                                        else if (mimeTypeInt == index) MaterialTheme.colorScheme.onMixedColor
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = modifier
                    .block(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    )
                    .offset(x = 0.dp, y = 9.dp)
                    .padding(start = 4.dp, end = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.extension),
                    Modifier
                        .offset(x = 0.dp, y = (-1).dp)
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                CompositionLocalProvider(
                    LocalRippleTheme provides GroupRipple
                ) {
                    Column {
                        listOf("JPG", "WEBP").forEachIndexed { index, item ->
                            OutlinedButton(
                                enabled = enabled,
                                onClick = { onMimeChange(index) },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .width(80.dp)
                                    .then(
                                        when (index) {
                                            0 -> Modifier.zIndex(if (mimeTypeInt == 0) 1f else 0f)

                                            else ->
                                                Modifier
                                                    .offset(y = (-9).dp)
                                                    .zIndex(if (mimeTypeInt == index) 1f else 0f)
                                        }
                                    ),
                                shape = when (index) {
                                    0 -> RoundedCornerShape(
                                        topStart = cornerRadius,
                                        topEnd = cornerRadius,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )

                                    else -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                },
                                border = BorderStroke(
                                    max(settingsState.borderWidth, 1.dp),
                                    MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!enabled) disColor
                                    else if (mimeTypeInt == index) MaterialTheme.colorScheme.mixedColor
                                    else Color.Transparent
                                )
                            ) {
                                AutoSizeText(
                                    text = item,
                                    color = if (!enabled) disColor
                                    else if (mimeTypeInt == index) MaterialTheme.colorScheme.onMixedColor
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    maxLines = 1
                                )
                            }
                        }
                        listOf("JPEG", "PNG").forEachIndexed { i, item ->
                            val index = i + 2
                            OutlinedButton(
                                enabled = enabled,
                                onClick = { onMimeChange(index) },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .width(80.dp)
                                    .then(
                                        when (index) {
                                            2 ->
                                                Modifier
                                                    .offset(y = (-18).dp)
                                                    .zIndex(if (mimeTypeInt == 0) 2f else 1f)

                                            else ->
                                                Modifier
                                                    .offset(y = (-24).dp)
                                                    .zIndex(if (mimeTypeInt == index) 2f else 1f)
                                        }
                                    ),
                                shape = when (index) {
                                    2 -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )

                                    else -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = cornerRadius,
                                        bottomEnd = cornerRadius
                                    )
                                },
                                border = BorderStroke(
                                    max(settingsState.borderWidth, 1.dp),
                                    MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!enabled) disColor
                                    else if (mimeTypeInt == index) MaterialTheme.colorScheme.mixedColor
                                    else Color.Transparent
                                )
                            ) {
                                AutoSizeText(
                                    text = item,
                                    color = if (!enabled) disColor
                                    else if (mimeTypeInt == index) MaterialTheme.colorScheme.onMixedColor
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}