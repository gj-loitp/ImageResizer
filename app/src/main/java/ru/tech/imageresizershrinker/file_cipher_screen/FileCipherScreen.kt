package ru.tech.imageresizershrinker.file_cipher_screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.twotone.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.file_cipher_screen.components.TipSheet
import ru.tech.imageresizershrinker.file_cipher_screen.viewModel.FileCipherViewModel
import ru.tech.imageresizershrinker.theme.Green
import ru.tech.imageresizershrinker.theme.icons.ShieldKey
import ru.tech.imageresizershrinker.theme.icons.ShieldOpen
import ru.tech.imageresizershrinker.theme.outlineVariant
import ru.tech.imageresizershrinker.utils.confetti.LocalConfettiController
import ru.tech.imageresizershrinker.utils.cipher.CipherUtils.randSalt
import ru.tech.imageresizershrinker.utils.cipher.CipherUtils.toByteArray
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.fileSize
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.shareFile
import ru.tech.imageresizershrinker.utils.helper.ContextUtils.getFileName
import ru.tech.imageresizershrinker.utils.helper.readableByteCount
import ru.tech.imageresizershrinker.utils.modifier.block
import ru.tech.imageresizershrinker.utils.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.utils.modifier.fabBorder
import ru.tech.imageresizershrinker.widget.other.LoadingDialog
import ru.tech.imageresizershrinker.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.widget.preferences.PreferenceRow
import ru.tech.imageresizershrinker.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.widget.buttons.ToggleGroupButton
import ru.tech.imageresizershrinker.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.widget.other.showError
import ru.tech.imageresizershrinker.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.widget.text.Marquee
import ru.tech.imageresizershrinker.widget.text.RoundedTextField
import ru.tech.imageresizershrinker.widget.utils.LocalSettingsState
import ru.tech.imageresizershrinker.widget.utils.isScrollingUp
import java.security.InvalidKeyException
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileCipherScreen(
    uriState: Uri?,
    onGoBack: () -> Unit,
    viewModel: FileCipherViewModel = viewModel()
) {
    LaunchedEffect(uriState) {
        uriState?.let { viewModel.setUri(it) }
    }

    val context = LocalContext.current
    val settingsState = LocalSettingsState.current
    val toastHostState = LocalToastHost.current
    val scope = rememberCoroutineScope()
    val confettiController = LocalConfettiController.current

    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val showTip = rememberSaveable { mutableStateOf(false) }

    var key by rememberSaveable { mutableStateOf("") }

    val onBack = {
        if (viewModel.uri != null && (key.isNotEmpty() || viewModel.byteArray != null)) showExitDialog =
            true
        else onGoBack()
    }
    var showSaveLoading by rememberSaveable { mutableStateOf(false) }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument(),
        onResult = {
            it?.let { uri ->
                showSaveLoading = true
                viewModel.saveCryptographyTo(
                    outputStream = context.contentResolver.openOutputStream(uri, "rw")
                ) { t ->
                    if (t != null) {
                        scope.launch {
                            toastHostState.showError(context, t)
                        }
                    } else {
                        scope.launch {
                            confettiController.showEmpty()
                        }
                        scope.launch {
                            toastHostState.showToast(
                                context.getString(
                                    R.string.saved_to,
                                    ""
                                ),
                                Icons.Rounded.Save
                            )
                        }
                    }
                    showSaveLoading = false
                }
            }
        }
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.setUri(it)
            }
        }
    )

    val focus = LocalFocusManager.current

    val state = rememberLazyListState()
    Box {

        Surface(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focus.clearFocus()
                    }
                )
            },
            color = MaterialTheme.colorScheme.background
        ) {
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            Box(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Column(Modifier.fillMaxSize()) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        modifier = Modifier.drawHorizontalStroke(),
                        title = {
                            Marquee(
                                edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            ) {
                                AnimatedContent(
                                    targetState = viewModel.uri to viewModel.isEncrypt,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                                ) { (uri, isEncrypt) ->
                                    Text(
                                        if (uri == null) {
                                            stringResource(R.string.cipher)
                                        } else {
                                            listOf(
                                                stringResource(R.string.encryption),
                                                stringResource(R.string.decryption)
                                            )[if (isEncrypt) 0 else 1]
                                        },
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            )
                        ),
                        navigationIcon = {
                            IconButton(onClick = onGoBack) {
                                Icon(Icons.Rounded.ArrowBack, null)
                            }
                        },
                        actions = {
                            TopAppBarEmoji()
                        }
                    )
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            bottom = 88.dp + WindowInsets
                                .navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding() + WindowInsets
                                .ime
                                .asPaddingValues()
                                .calculateBottomPadding(),
                            top = 20.dp,
                            end = 20.dp,
                            start = 20.dp
                        )
                    ) {
                        item {
                            AnimatedContent(targetState = viewModel.uri != null) { hasUri ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (!hasUri) {
                                        Column(
                                            modifier = Modifier.block(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Spacer(Modifier.height(16.dp))
                                            FilledIconButton(
                                                onClick = { filePicker.launch("*/*") },
                                                modifier = Modifier.size(100.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        6.dp
                                                    ),
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            ) {
                                                Icon(
                                                    Icons.TwoTone.FileOpen,
                                                    null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .border(
                                                            settingsState.borderWidth,
                                                            MaterialTheme.colorScheme.outlineVariant(
                                                                0.2f
                                                            ),
                                                            RoundedCornerShape(16.dp)
                                                        )
                                                        .padding(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                stringResource(R.string.pick_file_to_start),
                                                Modifier.padding(16.dp),
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Row(
                                            Modifier
                                                .block(MaterialTheme.shapes.extraLarge)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            ToggleGroupButton(
                                                enabled = true,
                                                items = listOf(
                                                    stringResource(R.string.encryption),
                                                    stringResource(R.string.decryption)
                                                ),
                                                title = null,
                                                selectedIndex = if (viewModel.isEncrypt) 0 else 1,
                                                indexChanged = {
                                                    viewModel.setIsEncrypt(it == 0)
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp, start = 2.dp)
                                            )
                                            OutlinedIconButton(
                                                onClick = {
                                                    showTip.value = true
                                                },
                                                colors = IconButtonDefaults.filledTonalIconButtonColors(),
                                                border = BorderStroke(
                                                    settingsState.borderWidth,
                                                    MaterialTheme.colorScheme.outlineVariant(onTopOf = MaterialTheme.colorScheme.secondaryContainer)
                                                ),
                                            ) {
                                                Icon(Icons.Rounded.HelpOutline, null)
                                            }
                                        }

                                        viewModel.uri?.let { uri ->
                                            PreferenceRow(
                                                modifier = Modifier.padding(top = 16.dp),
                                                title = context.getFileName(uri)
                                                    ?: stringResource(R.string.something_went_wrong),
                                                onClick = null,
                                                titleFontStyle = TextStyle(
                                                    lineHeight = 16.sp,
                                                    fontSize = 15.sp
                                                ),
                                                subtitle = viewModel.uri?.let {
                                                    stringResource(
                                                        id = R.string.size,
                                                        readableByteCount(
                                                            it.fileSize(context) ?: 0L
                                                        )
                                                    )
                                                },
                                                applyHorPadding = false,
                                                startContent = {
                                                    Icon(
                                                        Icons.Rounded.InsertDriveFile,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(
                                                            horizontal = 16.dp,
                                                            vertical = 8.dp
                                                        )
                                                    )
                                                }
                                            )

                                            FilledTonalButton(
                                                onClick = { filePicker.launch("*/*") },
                                                modifier = Modifier.padding(top = 16.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(),
                                                border = BorderStroke(
                                                    settingsState.borderWidth,
                                                    MaterialTheme.colorScheme.outlineVariant(onTopOf = MaterialTheme.colorScheme.secondaryContainer)
                                                ),
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Rounded.FolderOpen, null)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(text = stringResource(id = R.string.pick_file))
                                                }
                                            }

                                            RoundedTextField(
                                                modifier = Modifier
                                                    .padding(top = 16.dp)
                                                    .block(shape = RoundedCornerShape(24.dp))
                                                    .padding(8.dp),
                                                value = key,
                                                startIcon = {
                                                    IconButton(
                                                        onClick = {
                                                            key = randSalt(18)
                                                            viewModel.resetCalculatedData()
                                                        },
                                                        modifier = Modifier.padding(start = 4.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Rounded.Shuffle,
                                                            null,
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                },
                                                endIcon = {
                                                    IconButton(
                                                        onClick = {
                                                            key = ""
                                                            viewModel.resetCalculatedData()
                                                        },
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Outlined.Cancel,
                                                            null,
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                singleLine = false,
                                                onValueChange = {
                                                    key = it
                                                    viewModel.resetCalculatedData()
                                                },
                                                label = {
                                                    Text(stringResource(R.string.key))
                                                }
                                            )
                                        }

                                        Button(
                                            enabled = key.isNotEmpty(),
                                            onClick = {
                                                showSaveLoading = true
                                                viewModel.startCryptography(
                                                    key = key,
                                                    onFileRequest = { uri ->
                                                        context
                                                            .contentResolver
                                                            .openInputStream(uri)
                                                            ?.use { it.toByteArray() }
                                                    }
                                                ) {
                                                    if (it is InvalidKeyException) {
                                                        scope.launch {
                                                            toastHostState.showToast(
                                                                context.getString(R.string.invalid_password_or_not_encrypted),
                                                                Icons.Rounded.ErrorOutline
                                                            )
                                                        }
                                                    } else if (it != null) {
                                                        scope.launch {
                                                            toastHostState.showError(context, it)
                                                        }
                                                    }
                                                    showSaveLoading = false
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(top = 16.dp)
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            colors = ButtonDefaults.buttonColors(),
                                            border = BorderStroke(
                                                settingsState.borderWidth,
                                                MaterialTheme.colorScheme.outlineVariant(onTopOf = if (key.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Black)
                                            ),
                                        ) {
                                            AnimatedContent(
                                                targetState = viewModel.uri to viewModel.isEncrypt,
                                                transitionSpec = { fadeIn() togetherWith fadeOut() }
                                            ) { (uri, isEncrypt) ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    when {
                                                        uri == null -> {
                                                            Icon(Icons.Rounded.FileOpen, null)
                                                        }

                                                        isEncrypt -> {
                                                            Icon(Icons.Rounded.ShieldKey, null)
                                                        }

                                                        else -> {
                                                            Icon(Icons.Rounded.ShieldOpen, null)
                                                        }
                                                    }
                                                    Spacer(Modifier.width(8.dp))
                                                    when {
                                                        uri == null -> {
                                                            Text(
                                                                text = stringResource(R.string.pick_file),
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 16.sp
                                                            )
                                                        }

                                                        isEncrypt -> {
                                                            Text(
                                                                text = stringResource(R.string.encrypt),
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 16.sp
                                                            )
                                                        }

                                                        else -> {
                                                            Text(
                                                                text = stringResource(R.string.decrypt),
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 16.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        AnimatedVisibility(visible = viewModel.byteArray != null) {
                                            OutlinedCard(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 24.dp),
                                                border = BorderStroke(
                                                    settingsState.borderWidth,
                                                    MaterialTheme.colorScheme.outlineVariant()
                                                ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        10.dp
                                                    )
                                                ),
                                                shape = MaterialTheme.shapes.extraLarge
                                            ) {
                                                Column(Modifier.padding(16.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Rounded.CheckCircle,
                                                            null,
                                                            tint = Green,
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(
                                                                    color = MaterialTheme.colorScheme.surface,
                                                                    shape = CircleShape
                                                                )
                                                                .border(
                                                                    width = settingsState.borderWidth,
                                                                    color = MaterialTheme.colorScheme.outlineVariant(),
                                                                    shape = CircleShape
                                                                )
                                                                .padding(4.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Text(
                                                            stringResource(R.string.file_proceed),
                                                            fontSize = 17.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    Text(
                                                        text = stringResource(R.string.store_file_desc),
                                                        fontSize = 13.sp,
                                                        color = LocalContentColor.current.copy(alpha = 0.7f),
                                                        lineHeight = 14.sp,
                                                        modifier = Modifier.padding(vertical = 16.dp)
                                                    )
                                                    var name by rememberSaveable(viewModel.byteArray) {
                                                        mutableStateOf(
                                                            if (viewModel.isEncrypt) {
                                                                "enc-"
                                                            } else {
                                                                "dec-"
                                                            } + (viewModel.uri?.let {
                                                                context.getFileName(it)
                                                            } ?: Random.nextInt())
                                                        )
                                                    }
                                                    RoundedTextField(
                                                        modifier = Modifier
                                                            .padding(top = 8.dp)
                                                            .block(shape = RoundedCornerShape(24.dp))
                                                            .padding(8.dp),
                                                        value = name,
                                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                        singleLine = false,
                                                        onValueChange = { name = it },
                                                        label = {
                                                            Text(stringResource(R.string.filename))
                                                        }
                                                    )

                                                    Row(Modifier.padding(top = 24.dp)) {
                                                        FilledTonalButton(
                                                            onClick = {
                                                                saveLauncher.launch("*/*#$name")
                                                            },
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(50.dp),
                                                            colors = ButtonDefaults.filledTonalButtonColors(),
                                                            border = BorderStroke(
                                                                settingsState.borderWidth,
                                                                MaterialTheme.colorScheme.outlineVariant(
                                                                    onTopOf = MaterialTheme.colorScheme.secondaryContainer
                                                                )
                                                            ),
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.Rounded.FileDownload,
                                                                    null
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                AutoSizeText(
                                                                    text = stringResource(id = R.string.save),
                                                                    maxLines = 1
                                                                )
                                                            }
                                                        }
                                                        Spacer(Modifier.width(16.dp))
                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.byteArray?.let {
                                                                    showSaveLoading = true
                                                                    context.shareFile(
                                                                        byteArray = it,
                                                                        filename = name
                                                                    ) {
                                                                        scope.launch {
                                                                            confettiController.showEmpty()
                                                                        }
                                                                        showSaveLoading = false
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(50.dp),
                                                            colors = ButtonDefaults.filledTonalButtonColors(),
                                                            border = BorderStroke(
                                                                settingsState.borderWidth,
                                                                MaterialTheme.colorScheme.outlineVariant(
                                                                    onTopOf = MaterialTheme.colorScheme.secondaryContainer
                                                                )
                                                            ),
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(Icons.Rounded.Share, null)
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                AutoSizeText(
                                                                    text = stringResource(id = R.string.share),
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
                            }
                        }
                    }
                }

                if (showSaveLoading) LoadingDialog()
            }
        }


        if (viewModel.uri == null) {
            FloatingActionButton(
                onClick = {
                    filePicker.launch("*/*")
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(12.dp)
                    .align(settingsState.fabAlignment)
                    .fabBorder(),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            ) {
                val expanded =
                    if (settingsState.fabAlignment != Alignment.BottomCenter) state.isScrollingUp() else true
                val horizontalPadding by animateDpAsState(targetValue = if (expanded) 16.dp else 0.dp)

                Row(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.FileOpen, null)
                    AnimatedVisibility(visible = expanded) {
                        Row {
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.pick_file))
                        }
                    }
                }
            }
        }
    }

    ExitWithoutSavingDialog(
        onExit = onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    TipSheet(visible = showTip)

    BackHandler(onBack = onBack)

}

private class CreateDocument : ActivityResultContracts.CreateDocument("*/*") {
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(
            context = context,
            input = input.split("#")[0]
        ).putExtra(Intent.EXTRA_TITLE, input.split("#")[1])
    }
}