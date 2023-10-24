package ru.tech.imageresizershrinker.presentation.main_screen.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.core.APP_RELEASES
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedButton
import ru.tech.imageresizershrinker.presentation.root.widget.sheets.SimpleSheet
import ru.tech.imageresizershrinker.presentation.root.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.presentation.root.widget.text.HtmlText
import ru.tech.imageresizershrinker.presentation.root.widget.text.TitleItem

@Composable
fun UpdateSheet(changelog: String, tag: String, visible: MutableState<Boolean>) {
    val context = LocalContext.current

    SimpleSheet(
        endConfirmButtonPadding = 0.dp,
        visible = visible,
        title = {},
        sheetContent = {
            ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                Box {
                    Column(
                        modifier = Modifier.align(Alignment.TopCenter),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
                        ) {
                            CompositionLocalProvider(
                                LocalContentColor.provides(MaterialTheme.colorScheme.onSurface),
                                LocalTextStyle.provides(MaterialTheme.typography.bodyLarge)
                            ) {
                                TitleItem(
                                    text = stringResource(R.string.new_version, tag),
                                    icon = Icons.Rounded.NewReleases
                                )
                            }
                        }
                        HorizontalDivider()
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            HtmlText(
                                html = changelog.trimIndent(),
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
                            ) { uri ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
                            }
                        }
                    }
                    HorizontalDivider(Modifier.align(Alignment.BottomCenter))
                }
            }
        },
        confirmButton = {
            EnhancedButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("$APP_RELEASES/tag/${tag}")
                        )
                    )
                }
            ) {
                AutoSizeText(stringResource(id = R.string.update))
            }
        }
    )
}