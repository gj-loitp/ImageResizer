package ru.tech.imageresizershrinker.crop_screen


import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.size.Size
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.crop_screen.components.AspectRatioSelection
import ru.tech.imageresizershrinker.crop_screen.components.aspectRatios
import ru.tech.imageresizershrinker.crop_screen.viewModel.CropViewModel
import ru.tech.imageresizershrinker.theme.outlineVariant
import ru.tech.imageresizershrinker.utils.confetti.LocalConfettiController
import ru.tech.imageresizershrinker.utils.coil.filters.SaturationFilter
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.decodeBitmapByUri
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.shareBitmap
import ru.tech.imageresizershrinker.utils.helper.ContextUtils.requestStoragePermission
import ru.tech.imageresizershrinker.utils.helper.compressFormat
import ru.tech.imageresizershrinker.utils.helper.extension
import ru.tech.imageresizershrinker.utils.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.utils.modifier.fabBorder
import ru.tech.imageresizershrinker.utils.modifier.navBarsPaddingOnlyIfTheyAtTheBottom
import ru.tech.imageresizershrinker.utils.modifier.navBarsPaddingOnlyIfTheyAtTheEnd
import ru.tech.imageresizershrinker.utils.storage.LocalFileController
import ru.tech.imageresizershrinker.utils.storage.Picker
import ru.tech.imageresizershrinker.utils.storage.localImagePickerMode
import ru.tech.imageresizershrinker.utils.storage.rememberImagePicker
import ru.tech.imageresizershrinker.widget.other.LoadingDialog
import ru.tech.imageresizershrinker.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.widget.controls.ExtensionGroup
import ru.tech.imageresizershrinker.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.widget.other.showError
import ru.tech.imageresizershrinker.widget.text.Marquee
import ru.tech.imageresizershrinker.widget.utils.LocalSettingsState
import ru.tech.imageresizershrinker.widget.utils.LocalWindowSizeClass
import ru.tech.imageresizershrinker.widget.utils.isScrollingUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    uriState: Uri?,
    onGoBack: () -> Unit,
    viewModel: CropViewModel = hiltViewModel()
) {
    val settingsState = LocalSettingsState.current
    val context = LocalContext.current as ComponentActivity
    val toastHostState = LocalToastHost.current
    val themeState = LocalDynamicThemeState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val scope = rememberCoroutineScope()
    val confettiController = LocalConfettiController.current
    val showConfetti: () -> Unit = {
        scope.launch {
            confettiController.showEmpty()
        }
    }

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (viewModel.bitmap != null) showExitDialog = true
        else onGoBack()
    }

    LaunchedEffect(uriState) {
        uriState?.let {
            viewModel.setUri(it)
            context.decodeBitmapByUri(
                uri = it,
                onGetMimeType = viewModel::updateMimeType,
                onGetExif = {},
                onGetBitmap = { bmp ->
                    viewModel.updateBitmap(
                        bitmap = bmp, newBitmap = true
                    )
                },
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

    val pickImageLauncher =
        rememberImagePicker(
            mode = localImagePickerMode(Picker.Single)
        ) { uris ->
            uris.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                viewModel.setUri(it)
                context.decodeBitmapByUri(
                    uri = it,
                    onGetMimeType = {},
                    onGetExif = {},
                    onGetBitmap = { bmp ->
                        viewModel.updateBitmap(
                            bitmap = bmp, newBitmap = true
                        )
                    },
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

    var showSaveLoading by rememberSaveable { mutableStateOf(false) }

    val fileController = LocalFileController.current
    val saveBitmap: (Bitmap) -> Unit = {
        showSaveLoading = true
        viewModel.saveBitmap(
            bitmap = it,
            fileController = fileController,
        ) { success ->
            if (!success) context.requestStoragePermission()
            else {
                scope.launch {
                    toastHostState.showToast(
                        context.getString(
                            R.string.saved_to,
                            fileController.savingPath
                        ),
                        Icons.Rounded.Save
                    )
                }
                showConfetti()
            }
            showSaveLoading = false
        }
    }


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()

    val portrait =
        LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE || LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            confirmValueChange = {
                when (it) {
                    SheetValue.Hidden -> false
                    else -> true
                }
            }
        )
    )

    var crop by remember { mutableStateOf(false) }
    var share by remember { mutableStateOf(false) }
    val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (viewModel.bitmap == null) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        modifier = Modifier.drawHorizontalStroke(),
                        title = {
                            Marquee(
                                edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            ) {
                                Text(stringResource(R.string.crop))
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            )
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = onBack
                            ) {
                                Icon(Icons.Rounded.ArrowBack, null)
                            }
                        },
                        actions = {
                            TopAppBarEmoji()
                        }
                    )
                } else {
                    TopAppBar(
                        modifier = Modifier.drawHorizontalStroke(),
                        title = {
                            Marquee(
                                edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            ) {
                                Text(stringResource(R.string.crop))
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            )
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = onBack
                            ) {
                                Icon(Icons.Rounded.ArrowBack, null)
                            }
                        },
                        actions = {
                            if (portrait) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                                scaffoldState.bottomSheetState.partialExpand()
                                            } else {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        }
                                    },
                                ) {
                                    Icon(Icons.Rounded.Build, null)
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.resetBitmap()
                                },
                                enabled = viewModel.bitmap != null && viewModel.isBitmapChanged
                            ) {
                                Icon(Icons.Outlined.RestartAlt, null)
                            }
                            IconButton(
                                onClick = {
                                    share = true
                                    crop = true
                                },
                                enabled = viewModel.bitmap != null
                            ) {
                                Icon(Icons.Outlined.Share, null)
                            }
                        }
                    )
                }
                viewModel.bitmap?.let {
                    if (portrait) {
                        Column {
                            AnimatedContent(
                                targetState = (viewModel.cropProperties.aspectRatio != AspectRatio.Original) to it,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            ) { (fixedAspectRatio, bitmap) ->
                                val bmp = remember(bitmap) { it.asImageBitmap() }
                                ImageCropper(
                                    background = MaterialTheme.colorScheme.surface,
                                    imageBitmap = bmp,
                                    contentDescription = null,
                                    cropProperties = viewModel.cropProperties.copy(fixedAspectRatio = fixedAspectRatio),
                                    onCropStart = {
                                        viewModel.imageCropStarted()
                                    },
                                    crop = crop,
                                    cropStyle = CropDefaults.style(
                                        overlayColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    onCropSuccess = { image ->
                                        viewModel.imageCropFinished()
                                        if (share) {
                                            showSaveLoading = true
                                            context.shareBitmap(
                                                bitmap = image.asAndroidBitmap(),
                                                compressFormat = viewModel.mimeType.extension.compressFormat,
                                                onComplete = {
                                                    showConfetti()
                                                    showSaveLoading = false
                                                }
                                            )
                                        } else {
                                            viewModel.updateBitmap(image.asAndroidBitmap())
                                        }
                                        crop = false
                                        share = false
                                    },
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.navBarsPaddingOnlyIfTheyAtTheEnd(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.weight(0.8f)
                            ) {
                                AnimatedContent(
                                    targetState = (viewModel.cropProperties.aspectRatio != AspectRatio.Original) to it,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize()
                                ) { (fixedAspectRatio, bitmap) ->
                                    val bmp = remember(bitmap) { it.asImageBitmap() }
                                    ImageCropper(
                                        background = MaterialTheme.colorScheme.surface,
                                        imageBitmap = bmp,
                                        contentDescription = null,
                                        cropStyle = CropDefaults.style(
                                            overlayColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        cropProperties = viewModel.cropProperties.copy(
                                            fixedAspectRatio = fixedAspectRatio
                                        ),
                                        onCropStart = {
                                            viewModel.imageCropStarted()
                                        },
                                        crop = crop,
                                        onCropSuccess = { image ->
                                            viewModel.imageCropFinished()
                                            if (share) {
                                                showSaveLoading = true
                                                context.shareBitmap(
                                                    bitmap = image.asAndroidBitmap(),
                                                    compressFormat = viewModel.mimeType.extension.compressFormat,
                                                    onComplete = {
                                                        showConfetti()
                                                        showSaveLoading = false
                                                    }
                                                )
                                            } else {
                                                viewModel.updateBitmap(image.asAndroidBitmap())
                                                showConfetti()
                                            }
                                            crop = false
                                            share = false
                                        }
                                    )
                                }
                            }
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .width(settingsState.borderWidth.coerceAtLeast(0.25.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant())
                            )
                            val aspectRatios = aspectRatios()
                            AspectRatioSelection(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                horizontal = false,
                                selectedIndex = aspectRatios.indexOfFirst { cr ->
                                    cr.aspectRatio == viewModel.cropProperties.aspectRatio
                                }
                            ) { aspect ->
                                viewModel.setCropAspectRatio(aspect.aspectRatio)
                            }
                            ExtensionGroup(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .navBarsPaddingOnlyIfTheyAtTheBottom(),
                                orientation = Orientation.Vertical,
                                enabled = viewModel.bitmap != null,
                                mimeTypeInt = viewModel.mimeType,
                                onMimeChange = {
                                    viewModel.updateMimeType(it)
                                }
                            )
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .width(settingsState.borderWidth.coerceAtLeast(0.25.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant())
                                    .padding(start = 20.dp)
                            )
                            Column(
                                Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxHeight()
                                    .navigationBarsPadding(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                FloatingActionButton(
                                    onClick = pickImage,
                                    modifier = Modifier.fabBorder(),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                    content = {
                                        Icon(Icons.Rounded.AddPhotoAlternate, null)
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                var job by remember { mutableStateOf<Job?>(null) }
                                FloatingActionButton(
                                    onClick = {
                                        job?.cancel()
                                        job = scope.launch {
                                            kotlinx.coroutines.delay(500)
                                            crop = true
                                        }
                                    },
                                    modifier = Modifier.fabBorder(),
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                ) {
                                    Icon(Icons.Rounded.Crop, null)
                                }
                                AnimatedVisibility(viewModel.isBitmapChanged) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        FloatingActionButton(
                                            onClick = {
                                                viewModel.bitmap?.let(saveBitmap)
                                            },
                                            modifier = Modifier.fabBorder(),
                                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                        ) {
                                            Icon(Icons.Rounded.Save, null)
                                        }
                                    }
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

            if (viewModel.bitmap == null) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .align(settingsState.fabAlignment)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = pickImage,
                        modifier = Modifier.fabBorder(),
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
        }
    }

    if (portrait && viewModel.bitmap != null) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 80.dp + WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding(),
            sheetDragHandle = null,
            sheetShape = RectangleShape,
            sheetContent = {
                BottomAppBar(
                    modifier = Modifier.drawHorizontalStroke(true),
                    actions = {
                        var job by remember { mutableStateOf<Job?>(null) }
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = BorderStroke(
                                settingsState.borderWidth,
                                MaterialTheme.colorScheme.outlineVariant(onTopOf = MaterialTheme.colorScheme.secondaryContainer)
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = {
                                job?.cancel()
                                job = scope.launch {
                                    kotlinx.coroutines.delay(500)
                                    crop = true
                                }
                            }
                        ) {
                            Text(stringResource(R.string.crop))
                        }
                    },
                    floatingActionButton = {
                        Row {
                            FloatingActionButton(
                                onClick = pickImage,
                                modifier = Modifier.fabBorder(),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                            ) {
                                val expanded =
                                    scrollState.isScrollingUp() && viewModel.bitmap == null
                                val horizontalPadding by animateDpAsState(targetValue = if (expanded) 16.dp else 0.dp)
                                Row(
                                    modifier = Modifier.padding(horizontal = horizontalPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null)
                                    AnimatedVisibility(visible = expanded) {
                                        Row {
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.pick_image_alt))
                                        }
                                    }
                                }
                            }
                            AnimatedVisibility(viewModel.isBitmapChanged) {
                                Row {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    FloatingActionButton(
                                        onClick = {
                                            viewModel.bitmap?.let(saveBitmap)
                                        },
                                        modifier = Modifier.fabBorder(),
                                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                    ) {
                                        Icon(Icons.Rounded.Save, null)
                                    }
                                }
                            }
                        }
                    }
                )
                Divider()
                val aspectRatios = aspectRatios()
                AspectRatioSelection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    selectedIndex = aspectRatios.indexOfFirst { cr ->
                        cr.aspectRatio == viewModel.cropProperties.aspectRatio
                    }
                ) { aspect ->
                    viewModel.setCropAspectRatio(aspect.aspectRatio)
                }
                Divider()
                ExtensionGroup(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    orientation = Orientation.Horizontal,
                    enabled = viewModel.bitmap != null,
                    mimeTypeInt = viewModel.mimeType,
                    onMimeChange = {
                        viewModel.updateMimeType(it)
                    }
                )
            },
            content = content
        )
    } else {
        content(PaddingValues())
    }

    if (showSaveLoading || viewModel.isLoading) {
        LoadingDialog()
    }

    ExitWithoutSavingDialog(
        onExit = onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    BackHandler(onBack = onBack)
}