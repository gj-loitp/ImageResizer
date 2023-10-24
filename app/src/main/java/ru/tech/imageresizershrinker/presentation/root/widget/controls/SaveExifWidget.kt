package ru.tech.imageresizershrinker.presentation.root.widget.controls

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.domain.model.ImageFormat
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container

@Composable
fun SaveExifWidget(
    selected: Boolean,
    imageFormat: ImageFormat,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
) {
    Row(
        modifier = modifier
            .container(
                shape = RoundedCornerShape(24.dp),
                resultPadding = 0.dp,
                color = backgroundColor
            )
            .then(
                if (imageFormat.canWriteExif) {
                    Modifier.clickable { onCheckedChange(!selected) }
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Fingerprint, null, modifier = Modifier.defaultMinSize(24.dp, 24.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.keep_exif),
                fontWeight = FontWeight.Medium
            )
            AnimatedContent(
                targetState = imageFormat.canWriteExif,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { canWriteExif ->
                Text(
                    text = if (canWriteExif) {
                        stringResource(R.string.keep_exif_sub)
                    } else {
                        stringResource(
                            R.string.image_exif_warning,
                            imageFormat.title
                        )
                    },
                    fontWeight = FontWeight.Normal,
                    color = LocalContentColor.current.copy(0.5f),
                    lineHeight = 12.sp,
                    fontSize = 12.sp
                )
            }
        }
        AnimatedVisibility(
            visible = imageFormat.canWriteExif,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Switch(
                checked = selected,
                onCheckedChange = onCheckedChange
            )
        }
    }
}