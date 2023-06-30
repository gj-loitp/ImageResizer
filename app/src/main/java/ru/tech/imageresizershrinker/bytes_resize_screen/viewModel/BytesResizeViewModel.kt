package ru.tech.imageresizershrinker.bytes_resize_screen.viewModel

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.common.SAVE_FOLDER
import ru.tech.imageresizershrinker.utils.helper.BitmapInfo
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.canShow
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.copyTo
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.resizeBitmap
import ru.tech.imageresizershrinker.utils.helper.BitmapUtils.scaleByMaxBytes
import ru.tech.imageresizershrinker.utils.helper.compressFormat
import ru.tech.imageresizershrinker.utils.helper.extension
import ru.tech.imageresizershrinker.utils.helper.mimeTypeInt
import ru.tech.imageresizershrinker.utils.storage.BitmapSaveTarget
import ru.tech.imageresizershrinker.utils.storage.FileController
import ru.tech.imageresizershrinker.utils.storage.SavingFolder
import javax.inject.Inject

@HiltViewModel
class BytesResizeViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _canSave = mutableStateOf(false)
    val canSave by _canSave

    private val _presetSelected: MutableState<Int> = mutableIntStateOf(-1)
    val presetSelected by _presetSelected

    private val _handMode = mutableStateOf(true)
    val handMode by _handMode

    private val _uris = mutableStateOf<List<Uri>?>(null)
    val uris by _uris

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    private val _keepExif = mutableStateOf(false)
    val keepExif by _keepExif

    private val _isLoading: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: Boolean by _isLoading

    private val _previewBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val previewBitmap: Bitmap? by _previewBitmap

    private val _done: MutableState<Int> = mutableIntStateOf(0)
    val done by _done

    private val _selectedUri: MutableState<Uri?> = mutableStateOf(null)
    val selectedUri by _selectedUri

    private val _maxBytes: MutableState<Long> = mutableLongStateOf(0L)
    val maxBytes by _maxBytes

    private val _mime: MutableState<Int> = mutableIntStateOf(0)
    val mime by _mime

    fun setMime(mime: Int) {
        if (_mime.value != mime) {
            _mime.value = mime
        }
    }

    fun updateUris(uris: List<Uri>?) {
        _uris.value = null
        _uris.value = uris
        _selectedUri.value = uris?.firstOrNull()
    }

    fun updateUrisSilently(
        removedUri: Uri,
        loader: suspend (Uri) -> Bitmap?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _uris.value = uris
                if (_selectedUri.value == removedUri) {
                    val index = uris?.indexOf(removedUri) ?: -1
                    if (index == 0) {
                        uris?.getOrNull(1)?.let {
                            _selectedUri.value = it
                            _bitmap.value = loader(it)
                        }
                    } else {
                        uris?.getOrNull(index - 1)?.let {
                            _selectedUri.value = it
                            _bitmap.value = loader(it)
                        }
                    }
                }
                val u = _uris.value?.toMutableList()?.apply {
                    remove(removedUri)
                }
                _uris.value = u
            }
        }
    }


    fun updateBitmap(bitmap: Bitmap?) {
        viewModelScope.launch {
            _isLoading.value = true
            _bitmap.value = bitmap
            var bmp: Bitmap?
            withContext(Dispatchers.IO) {
                bmp = if (bitmap?.canShow() == false) {
                    bitmap.resizeBitmap(
                        height_ = (bitmap.height * 0.9f).toInt(),
                        width_ = (bitmap.width * 0.9f).toInt(),
                        resize = 1
                    )
                } else bitmap

                while (bmp?.canShow() == false) {
                    bmp = bmp?.resizeBitmap(
                        height_ = (bmp!!.height * 0.9f).toInt(),
                        width_ = (bmp!!.width * 0.9f).toInt(),
                        resize = 1
                    )
                }
            }
            _previewBitmap.value = bmp
            _isLoading.value = false
        }
    }

    fun setKeepExif(boolean: Boolean) {
        _keepExif.value = boolean
    }

    fun saveBitmaps(
        fileController: FileController,
        getBitmap: suspend (Uri) -> Pair<Bitmap?, ExifInterface?>,
        onResult: (Int) -> Unit
    ) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            var failed = 0
            if (!fileController.isExternalStorageWritable()) {
                onResult(-1)
            } else {
                _done.value = 0
                uris?.forEach { uri ->
                    runCatching {
                        getBitmap(uri)
                    }.getOrNull()?.takeIf { it.first != null }?.let { (bitmap, exif) ->
                        kotlin.runCatching {
                            if (handMode) {
                                bitmap?.scaleByMaxBytes(
                                    maxBytes = maxBytes,
                                    compressFormat = mime.extension.compressFormat
                                )
                            } else {
                                bitmap?.scaleByMaxBytes(
                                    maxBytes = (fileController.getSize(uri) ?: 0)
                                        .times(_presetSelected.value / 100f)
                                        .toLong(),
                                    compressFormat = mime.extension.compressFormat
                                )
                            }
                        }.let { result ->
                            if (result.isSuccess && result.getOrNull() != null) {
                                val scaled = result.getOrNull()!!
                                val localBitmap = scaled.first

                                val writeTo: (SavingFolder) -> Unit = { savingFolder ->
                                    savingFolder.outputStream?.use { outputStream ->
                                        localBitmap.compress(
                                            mime.extension.compressFormat,
                                            scaled.second,
                                            outputStream
                                        )
                                        if (keepExif) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                val fd =
                                                    fileController.getFileDescriptorFor(savingFolder.fileUri)
                                                fd?.fileDescriptor?.let {
                                                    val ex = ExifInterface(it)
                                                    exif?.copyTo(ex)
                                                    ex.saveAttributes()
                                                }
                                                fd?.close()
                                            } else {
                                                val image = savingFolder.file!!
                                                val ex = ExifInterface(image)
                                                exif?.copyTo(ex)
                                                ex.saveAttributes()
                                            }
                                        }
                                    }
                                }
                                fileController.getSavingFolder(
                                    BitmapSaveTarget(
                                        bitmapInfo = BitmapInfo(
                                            mimeTypeInt = mime.extension.mimeTypeInt,
                                            width = localBitmap.width,
                                            height = localBitmap.height
                                        ),
                                        uri = uri,
                                        sequenceNumber = _done.value + 1
                                    )
                                ).getOrNull()?.let(writeTo) ?: dataStore.edit {
                                    it[SAVE_FOLDER] = ""
                                }
                            } else failed += 1
                        }
                    }
                    _done.value += 1
                }
                onResult(failed)
            }
        }
    }

    fun setBitmap(loader: suspend () -> Bitmap?, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateBitmap(loader())
                _selectedUri.value = uri
            }
        }
    }

    private fun updateCanSave() {
        _canSave.value =
            _bitmap.value != null && (_maxBytes.value != 0L && _handMode.value || !_handMode.value && _presetSelected.value != -1)
    }

    fun updateMaxBytes(newBytes: String) {
        val b = newBytes.toLongOrNull() ?: 0
        _maxBytes.value = b * 1024
        updateCanSave()
    }

    fun selectPreset(preset: Int) {
        _presetSelected.value = preset
        updateCanSave()
    }

    fun updateHandMode() {
        _handMode.value = !_handMode.value
        updateCanSave()
    }

    fun proceedBitmap(
        uri: Uri,
        bitmapResult: Result<Bitmap?>,
        getImageSize: (Uri) -> Long?
    ): Pair<Bitmap, BitmapInfo>? {
        return bitmapResult.getOrNull()?.let { bitmap ->
            kotlin.runCatching {
                if (handMode) {
                    bitmap.scaleByMaxBytes(
                        maxBytes = maxBytes,
                        compressFormat = mime.extension.compressFormat
                    )
                } else {
                    bitmap.scaleByMaxBytes(
                        maxBytes = (getImageSize(uri) ?: 0)
                            .times(_presetSelected.value / 100f)
                            .toLong(),
                        compressFormat = mime.extension.compressFormat
                    )
                }
            }
        }?.let { result ->
            if (result.isSuccess && result.getOrNull() != null) {
                val scaled = result.getOrNull()!!
                scaled.first to BitmapInfo(
                    mimeTypeInt = _mime.value,
                    quality = scaled.second.toFloat(),
                    width = scaled.first.width,
                    height = scaled.first.height
                )
            } else null
        }
    }

    fun setProgress(progress: Int) {
        _done.value = progress
    }

}