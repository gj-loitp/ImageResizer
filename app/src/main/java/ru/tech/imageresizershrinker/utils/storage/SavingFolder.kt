package ru.tech.imageresizershrinker.utils.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.utils.helper.ContextUtils.getFileName
import ru.tech.imageresizershrinker.utils.helper.extension
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class SavingFolder(
    val outputStream: OutputStream? = null,
    val file: File? = null,
    val fileUri: Uri? = null,
)

fun Uri.toPath(
    context: Context,
    isTreeUri: Boolean = true
): String? {
    return if (isTreeUri) {
        DocumentFile.fromTreeUri(context, this)
    } else {
        DocumentFile.fromSingleUri(context, this)
    }?.uri?.path?.split(":")?.lastOrNull()
}

fun Uri?.toUiPath(context: Context, default: String): String = this?.let { uri ->
    DocumentFile
        .fromTreeUri(context, uri)
        ?.uri?.path?.split(":")
        ?.lastOrNull()?.let { p ->
            val endPath = p.takeIf {
                it.isNotEmpty()
            }?.let { "/$it" } ?: ""
            val startPath = if (
                uri.toString()
                    .split("%")[0]
                    .contains("primary")
            ) context.getString(R.string.device_storage)
            else context.getString(R.string.external_storage)

            startPath + endPath
        }
} ?: default

fun defaultPrefix() = "ResizedImage"

fun constructFilename(
    context: Context,
    fileParams: FileParams,
    saveTarget: BitmapSaveTarget
): String {
    val wh = "(" + (if (saveTarget.uri == Uri.EMPTY) context.getString(R.string.width)
        .split(" ")[0] else saveTarget.bitmapInfo.width) + ")x(" + (if (saveTarget.uri == Uri.EMPTY) context.getString(
        R.string.height
    ).split(" ")[0] else saveTarget.bitmapInfo.height) + ")"

    var prefix = fileParams.filenamePrefix
    val extension = saveTarget.bitmapInfo.mimeTypeInt.extension

    if (prefix.isEmpty()) prefix = defaultPrefix()

    if (fileParams.addOriginalFilename) prefix += "_${
        if (saveTarget.uri != Uri.EMPTY) {
            context.getFileName(saveTarget.uri) ?: ""
        } else {
            context.getString(R.string.original_filename)
        }
    }"
    if (fileParams.addSizeInFilename) prefix += wh

    val timeStamp = SimpleDateFormat(
        "yyyy-MM-dd_HH-mm-ss",
        Locale.getDefault()
    ).format(Date()) + "_${Random(Random.nextInt()).hashCode().toString().take(4)}"
    return "${prefix}_${
        if (fileParams.addSequenceNumber && saveTarget.sequenceNumber != null) {
            SimpleDateFormat(
                "yyyy-MM-dd_HH-mm-ss",
                Locale.getDefault()
            ).format(Date()) + "_" + saveTarget.sequenceNumber
        } else if (saveTarget.uri == Uri.EMPTY && fileParams.addSequenceNumber) {
            SimpleDateFormat(
                "yyyy-MM-dd_HH-mm-ss",
                Locale.getDefault()
            ).format(Date()) + "_" + context.getString(R.string.sequence_num)
        } else {
            timeStamp
        }
    }.$extension"
}

fun Context.getSavingFolder(
    treeUri: Uri?,
    saveTarget: SaveTarget
): SavingFolder {
    return if (treeUri == null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, saveTarget.filename)
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    saveTarget.mimeType
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "DCIM/ResizedImages"
                )
            }
            val imageUri = contentResolver.insert(
                if ("image" in saveTarget.mimeType) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" in saveTarget.mimeType) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" in saveTarget.mimeType) {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                },
                contentValues
            )

            SavingFolder(
                outputStream = contentResolver.openOutputStream(imageUri!!),
                fileUri = imageUri
            )
        } else {
            val imagesDir = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM
                ), "ResizedImages"
            )
            if (!imagesDir.exists()) imagesDir.mkdir()
            SavingFolder(
                outputStream = FileOutputStream(File(imagesDir, saveTarget.filename!!)),
                file = File(imagesDir, saveTarget.filename!!)
            )
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val documentFile = DocumentFile.fromTreeUri(this, treeUri)

            if (documentFile?.exists() == false || documentFile == null) {
                throw NoSuchFileException(File(treeUri.toString()))
            }

            val file = documentFile.createFile(saveTarget.mimeType, saveTarget.filename!!)

            val imageUri = file!!.uri
            SavingFolder(
                outputStream = contentResolver.openOutputStream(imageUri),
                fileUri = imageUri
            )
        } else {
            val path = treeUri.toPath(this@getSavingFolder)?.split("/")?.let {
                it - it.last() to it.last()
            }
            val imagesDir = File(
                Environment.getExternalStoragePublicDirectory(
                    "${path?.first?.joinToString("/")}"
                ), path?.second.toString()
            )
            if (!imagesDir.exists()) imagesDir.mkdir()
            SavingFolder(
                outputStream = FileOutputStream(File(imagesDir, saveTarget.filename!!)),
                file = File(imagesDir, saveTarget.filename!!)
            )
        }
    }
}