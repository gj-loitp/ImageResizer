package ru.tech.imageresizershrinker.bytes_resize_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.widget.utils.LocalSettingsState

@Composable
fun PngTypeAlert(visible: Boolean) {
    val settingsState = LocalSettingsState.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandIn(expandFrom = Alignment.TopCenter),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopCenter)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                    alpha = 0.7f
                ),
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier
                .padding(16.dp)
                .border(
                    settingsState.borderWidth,
                    MaterialTheme.colorScheme.onErrorContainer.copy(
                        0.4f
                    ),
                    RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = stringResource(R.string.png_warning_bytes),
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp,
                color = LocalContentColor.current.copy(alpha = 0.5f)
            )
        }
    }
}