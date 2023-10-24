package ru.tech.imageresizershrinker.presentation.root.widget.controls.resize_group

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.domain.model.ResizeType
import ru.tech.imageresizershrinker.presentation.draw_screen.components.DrawBackgroundSelector
import ru.tech.imageresizershrinker.presentation.root.widget.buttons.ToggleGroupButton
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedButton
import ru.tech.imageresizershrinker.presentation.root.widget.controls.resize_group.components.BlurRadiusSelector
import ru.tech.imageresizershrinker.presentation.root.widget.controls.resize_group.components.UseBlurredBackgroundToggle
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container
import ru.tech.imageresizershrinker.presentation.root.widget.saver.ColorSaver
import ru.tech.imageresizershrinker.presentation.root.widget.sheets.SimpleSheet
import ru.tech.imageresizershrinker.presentation.root.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.presentation.root.widget.text.TitleItem


@Composable
fun ResizeGroup(
    enabled: Boolean,
    resizeType: ResizeType,
    onResizeChange: (ResizeType) -> Unit
) {
    val state = rememberSaveable { mutableStateOf(false) }
    var canvasColor by rememberSaveable(stateSaver = ColorSaver) {
        mutableStateOf(Color.Transparent)
    }
    var useBlurredBgInsteadOfColor by rememberSaveable {
        mutableStateOf(true)
    }
    var blurRadius by remember {
        mutableIntStateOf(35)
    }

    val modifiedResizeType by remember(canvasColor, useBlurredBgInsteadOfColor, blurRadius) {
        derivedStateOf {
            ResizeType.CenterCrop(
                canvasColor = canvasColor.toArgb()
                    .takeIf { !useBlurredBgInsteadOfColor },
                blurRadius = blurRadius
            )
        }
    }
    val updateResizeType = {
        onResizeChange(modifiedResizeType)
    }
    Column(
        modifier = Modifier
            .container(shape = RoundedCornerShape(24.dp))
    ) {
        ToggleGroupButton(
            modifier = Modifier.padding(start = 3.dp, end = 2.dp),
            enabled = enabled,
            title = {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.resize_type),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                state.value = true
                            }
                            .padding(1.dp)
                            .size(
                                with(LocalDensity.current) {
                                    LocalTextStyle.current.fontSize.toDp()
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            },
            items = listOf(
                stringResource(R.string.explicit),
                stringResource(R.string.flexible),
                stringResource(R.string.crop),
                stringResource(R.string.ratio)
            ),
            selectedIndex = when (resizeType) {
                ResizeType.Explicit -> 0
                ResizeType.Flexible -> 1
                is ResizeType.CenterCrop -> 2
                ResizeType.Ratio -> 3
                else -> throw IllegalStateException()
            },
            indexChanged = {
                onResizeChange(
                    when (it) {
                        0 -> ResizeType.Explicit
                        1 -> ResizeType.Flexible
                        2 -> modifiedResizeType
                        3 -> ResizeType.Ratio
                        else -> throw IllegalStateException()
                    }
                )
            }
        )
        AnimatedVisibility(
            visible = resizeType is ResizeType.CenterCrop,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            UseBlurredBackgroundToggle(
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                selected = useBlurredBgInsteadOfColor,
                onCheckedChange = {
                    useBlurredBgInsteadOfColor = it
                    updateResizeType()
                }
            )
        }
        AnimatedVisibility(
            visible = resizeType is ResizeType.CenterCrop,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AnimatedContent(targetState = useBlurredBgInsteadOfColor) { showBlurRadius ->
                if (!showBlurRadius) {
                    DrawBackgroundSelector(
                        modifier = Modifier
                            .padding(bottom = 8.dp, end = 8.dp, start = 8.dp)
                            .container(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            ),
                        value = canvasColor,
                        onColorChange = {
                            canvasColor = it
                            updateResizeType()
                        }
                    )
                } else {
                    BlurRadiusSelector(
                        modifier = Modifier.padding(bottom = 8.dp, end = 8.dp, start = 8.dp),
                        value = blurRadius,
                        onValueChange = {
                            blurRadius = it
                            updateResizeType()
                        }
                    )
                }
            }
        }
    }

    SimpleSheet(
        sheetContent = {
            Box {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TitleItem(text = stringResource(R.string.explicit))
                    Text(
                        text = stringResource(R.string.explicit_description),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                    HorizontalDivider()

                    TitleItem(text = stringResource(R.string.flexible))
                    Text(
                        text = stringResource(R.string.flexible_description),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                    HorizontalDivider()

                    TitleItem(text = stringResource(R.string.crop))
                    Text(
                        text = stringResource(id = R.string.crop_description),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                    HorizontalDivider()

                    TitleItem(text = stringResource(R.string.ratio))
                    Text(
                        text = stringResource(id = R.string.ratio_description),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        visible = state,
        title = {
            TitleItem(text = stringResource(R.string.resize_type))
        },
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { state.value = false }
            ) {
                AutoSizeText(stringResource(R.string.close))
            }
        }
    )
}