package ru.tech.imageresizershrinker.presentation.main_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container
import ru.tech.imageresizershrinker.presentation.root.widget.text.TitleItem

@Composable
fun SettingItem(
    icon: ImageVector,
    text: String,
    initialState: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .animateContentSize()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            .container(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                resultPadding = 0.dp,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        var expanded by rememberSaveable { mutableStateOf(initialState) }
        val rotation by animateFloatAsState(if (expanded) 180f else 0f)
        TitleItem(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = !expanded }
                .padding(8.dp)
                .padding(start = 8.dp),
            icon = icon,
            text = text,
            endContent = {
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        )
        AnimatedVisibility(expanded) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                content()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}