package ru.tech.imageresizershrinker.pick_color_from_image_screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.size.Size
import com.smarttoolfactory.colordetector.ImageColorDetector
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.pick_color_from_image_screen.viewModel.PickColorViewModel
import ru.tech.imageresizershrinker.theme.icons.PaletteSwatch
import ru.tech.imageresizershrinker.theme.outlineVariant
import ru.tech.imageresizershrinker.utils.coil.filters.SaturationFilter
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.decodeBitmapByUri
import ru.tech.imageresizershrinker.utils.modifier.block
import ru.tech.imageresizershrinker.utils.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.utils.modifier.fabBorder
import ru.tech.imageresizershrinker.utils.modifier.navBarsPaddingOnlyIfTheyAtTheBottom
import ru.tech.imageresizershrinker.utils.modifier.navBarsPaddingOnlyIfTheyAtTheEnd
import ru.tech.imageresizershrinker.utils.navigation.LocalNavController
import ru.tech.imageresizershrinker.utils.navigation.Screen
import ru.tech.imageresizershrinker.utils.storage.Picker
import ru.tech.imageresizershrinker.utils.storage.localImagePickerMode
import ru.tech.imageresizershrinker.utils.storage.rememberImagePicker
import ru.tech.imageresizershrinker.widget.other.LoadingDialog
import ru.tech.imageresizershrinker.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.widget.other.showError
import ru.tech.imageresizershrinker.widget.text.Marquee
import ru.tech.imageresizershrinker.widget.utils.LocalSettingsState
import ru.tech.imageresizershrinker.widget.utils.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickColorFromImageScreen(
    uriState: Uri?,
    onGoBack: () -> Unit,
    viewModel: PickColorViewModel = viewModel()
) {
    val settingsState = LocalSettingsState.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val toastHostState = LocalToastHost.current
    val themeState = LocalDynamicThemeState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val scope = rememberCoroutineScope()

    var canZoom by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uriState) {
        uriState?.let {
            viewModel.setUri(it)
            context.decodeBitmapByUri(
                uri = it,
                originalSize = false,
                onGetMimeType = {},
                onGetExif = {},
                onGetBitmap = viewModel::updateBitmap,
                onError = {
                    scope.launch {
                        toastHostState.showError(context, it)
                    }
                }
            )
        }
    }

    LaunchedEffect(viewModel.bitmap) {
        viewModel.bitmap?.let {
            if (allowChangeColor) {
                themeState.updateColorByImage(
                    SaturationFilter(context, 2f).transform(it, Size.ORIGINAL)
                )
            }
        }
    }

    LaunchedEffect(viewModel.color) {
        if (!viewModel.color.isUnspecified) {
            if (allowChangeColor) themeState.updateColor(viewModel.color)
        }
    }

    val pickImageLauncher =
        rememberImagePicker(
            mode = localImagePickerMode(Picker.Single)
        ) { uris ->
            uris.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                viewModel.setUri(it)
                context.decodeBitmapByUri(
                    uri = it,
                    originalSize = false,
                    onGetMimeType = {},
                    onGetExif = {},
                    onGetBitmap = viewModel::updateBitmap,
                    onError = {
                        scope.launch {
                            toastHostState.showError(context, it)
                        }
                    }
                )
            }
        }

    val pickImage = {
        pickImageLauncher.pickImage()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()

    val portrait =
        LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE || LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact

    val switch = @Composable {
        Switch(
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            checked = !canZoom,
            onCheckedChange = { canZoom = !canZoom },
            thumbContent = {
                AnimatedContent(canZoom) { zoom ->
                    Icon(
                        if (!zoom) Icons.Rounded.Colorize else Icons.Rounded.ZoomIn,
                        null,
                        Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        val color = viewModel.color
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                modifier = Modifier.drawHorizontalStroke(),
                targetState = viewModel.bitmap == null,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { noBmp ->
                if (noBmp) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    onGoBack()
                                }
                            ) {
                                Icon(Icons.Rounded.ArrowBack, null)
                            }
                        },
                        title = {
                            Marquee(
                                edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            ) {
                                Text(stringResource(R.string.pick_color))
                            }
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ),
                        actions = {
                            if (viewModel.bitmap == null) {
                                TopAppBarEmoji()
                            }
                        }
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        modifier = Modifier.animateContentSize(),
                    ) {
                        Column {
                            Column(Modifier.navBarsPaddingOnlyIfTheyAtTheEnd()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            onGoBack()
                                        },
                                        modifier = Modifier.statusBarsPadding()
                                    ) {
                                        Icon(Icons.Rounded.ArrowBack, null)
                                    }
                                    if (!portrait) {
                                        ProvideTextStyle(
                                            value = LocalTextStyle.current.merge(
                                                MaterialTheme.typography.headlineSmall
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(
                                                        start = 16.dp,
                                                        end = 16.dp
                                                    )
                                                    .statusBarsPadding()
                                            ) {
                                                Text(stringResource(R.string.color))

                                                Text(
                                                    modifier = Modifier
                                                        .padding(horizontal = 8.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            context.copyColorIntoClipboard(
                                                                context.getString(R.string.color),
                                                                color.format()
                                                            )
                                                            scope.launch {
                                                                toastHostState.showToast(
                                                                    icon = Icons.Rounded.ContentPaste,
                                                                    message = context.getString(R.string.color_copied)
                                                                )
                                                            }
                                                        }
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .border(
                                                            settingsState.borderWidth,
                                                            MaterialTheme.colorScheme.outlineVariant(
                                                                onTopOf = MaterialTheme.colorScheme.secondaryContainer
                                                            ),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 6.dp),
                                                    text = color.format(),
                                                    style = LocalTextStyle.current.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                )

                                                Spacer(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(2.dp)
                                                )

                                                Box(
                                                    Modifier
                                                        .padding(
                                                            vertical = 4.dp,
                                                            horizontal = 16.dp
                                                        )
                                                        .background(
                                                            color = animateColorAsState(color).value,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .height(40.dp)
                                                        .width(72.dp)
                                                        .border(
                                                            width = settingsState.borderWidth,
                                                            color = MaterialTheme.colorScheme.outlineVariant(
                                                                onTopOf = animateColorAsState(color).value
                                                            ),
                                                            shape = RoundedCornerShape(11.dp)
                                                        )
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            context.copyColorIntoClipboard(
                                                                context.getString(R.string.color),
                                                                color.format()
                                                            )
                                                            scope.launch {
                                                                toastHostState.showToast(
                                                                    icon = Icons.Rounded.ContentPaste,
                                                                    message = context.getString(R.string.color_copied)
                                                                )
                                                            }
                                                        }
                                                )

                                                if (viewModel.uri != null) {
                                                    IconButton(
                                                        onClick = {
                                                            if (navController.backstack.entries.isNotEmpty()) navController.pop()
                                                            navController.navigate(
                                                                Screen.GeneratePalette(
                                                                    viewModel.uri
                                                                )
                                                            )
                                                        },
                                                        modifier = Modifier.statusBarsPadding()
                                                    ) {
                                                        Icon(Icons.Rounded.PaletteSwatch, null)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(
                                        Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp)
                                    )
                                    if (viewModel.uri != null && portrait) {
                                        IconButton(
                                            onClick = {
                                                if (navController.backstack.entries.isNotEmpty()) navController.pop()
                                                navController.navigate(
                                                    Screen.GeneratePalette(
                                                        viewModel.uri
                                                    )
                                                )
                                            },
                                            modifier = Modifier.statusBarsPadding()
                                        ) {
                                            Icon(Icons.Rounded.PaletteSwatch, null)
                                        }
                                    }
                                }
                                if (portrait) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ProvideTextStyle(
                                        value = LocalTextStyle.current.merge(
                                            MaterialTheme.typography.headlineSmall
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                        ) {
                                            Text(stringResource(R.string.color))

                                            Text(
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        context.copyColorIntoClipboard(
                                                            context.getString(R.string.color),
                                                            color.format()
                                                        )
                                                        scope.launch {
                                                            toastHostState.showToast(
                                                                icon = Icons.Rounded.ContentPaste,
                                                                message = context.getString(R.string.color_copied)
                                                            )
                                                        }
                                                    }
                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    .border(
                                                        settingsState.borderWidth,
                                                        MaterialTheme.colorScheme.outlineVariant(
                                                            onTopOf = MaterialTheme.colorScheme.secondaryContainer
                                                        ),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 6.dp),
                                                text = color.format(),
                                                style = LocalTextStyle.current.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            )

                                            Spacer(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(2.dp)
                                            )

                                            Box(
                                                Modifier
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        color = animateColorAsState(color).value,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .height(40.dp)
                                                    .width(72.dp)
                                                    .border(
                                                        width = settingsState.borderWidth,
                                                        color = MaterialTheme.colorScheme.outlineVariant(
                                                            onTopOf = animateColorAsState(color).value
                                                        ),
                                                        shape = RoundedCornerShape(11.dp)
                                                    )
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        context.copyColorIntoClipboard(
                                                            context.getString(R.string.color),
                                                            color.format()
                                                        )
                                                        scope.launch {
                                                            toastHostState.showToast(
                                                                icon = Icons.Rounded.ContentPaste,
                                                                message = context.getString(R.string.color_copied)
                                                            )
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                viewModel.bitmap?.let {
                    if (portrait) {
                        AnimatedContent(
                            targetState = it
                        ) { bitmap ->
                            ImageColorDetector(
                                canZoom = canZoom,
                                imageBitmap = bitmap.asImageBitmap(),
                                color = viewModel.color,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .navBarsPaddingOnlyIfTheyAtTheEnd()
                                    .block(RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                onColorChange = viewModel::updateColor
                            )
                        }
                    } else {
                        Row(Modifier.navBarsPaddingOnlyIfTheyAtTheEnd()) {
                            Box(
                                Modifier
                                    .weight(0.8f)
                                    .padding(20.dp)
                            ) {
                                Box(Modifier.align(Alignment.Center)) {
                                    AnimatedContent(
                                        targetState = it
                                    ) { bitmap ->
                                        ImageColorDetector(
                                            canZoom = canZoom,
                                            imageBitmap = bitmap.asImageBitmap(),
                                            color = viewModel.color,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .navBarsPaddingOnlyIfTheyAtTheBottom()
                                                .block(RoundedCornerShape(4.dp))
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            onColorChange = viewModel::updateColor
                                        )
                                    }
                                }
                            }
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .width(settingsState.borderWidth.coerceAtLeast(0.25.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant())
                            )
                            Column(
                                Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp)
                                    .navigationBarsPadding(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                switch()
                                Spacer(modifier = Modifier.height(16.dp))
                                FloatingActionButton(
                                    onClick = pickImage,
                                    modifier = Modifier.fabBorder(),
                                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                                ) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null)
                                }
                            }
                        }
                    }
                } ?: Column(Modifier.verticalScroll(scrollState)) {
                    ImageNotPickedWidget(
                        onPickImage = pickImage,
                        modifier = Modifier
                            .padding(bottom = 88.dp, top = 20.dp, start = 20.dp, end = 20.dp)
                            .navigationBarsPadding()
                    )
                }
            }
            if (viewModel.bitmap != null && portrait) {
                BottomAppBar(
                    modifier = Modifier
                        .drawHorizontalStroke(true),
                    actions = {
                        switch()
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = pickImage,
                            modifier = Modifier.fabBorder(),
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, null)
                        }
                    }
                )
            }
        }

        if (viewModel.bitmap == null) {
            ExtendedFloatingActionButton(
                onClick = pickImage,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .align(settingsState.fabAlignment)
                    .fabBorder(),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                text = {
                    Text(stringResource(R.string.pick_image_alt))
                },
                icon = {
                    Icon(Icons.Rounded.AddPhotoAlternate, null)
                }
            )
        }
    }

    if (viewModel.isLoading) LoadingDialog()

    BackHandler {
        onGoBack()
    }
}

fun Color.format(): String =
    String.format("#%08X", (0xFFFFFFFF and this.toArgb().toLong())).replace("#FF", "#")

fun Context.copyColorIntoClipboard(label: String, value: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, value)
    clipboard.setPrimaryClip(clip)
}