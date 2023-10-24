package ru.tech.imageresizershrinker.presentation.root.utils.navigation

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Margin
import androidx.compose.material.icons.outlined.PhotoSizeSelectSmall
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PhotoFilter
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.icons.material.CreateAlt
import ru.tech.imageresizershrinker.presentation.root.icons.material.FingerprintOff
import ru.tech.imageresizershrinker.presentation.root.icons.material.Interface
import ru.tech.imageresizershrinker.presentation.root.icons.material.PaletteSwatch
import ru.tech.imageresizershrinker.presentation.root.icons.material.Puzzle
import ru.tech.imageresizershrinker.presentation.root.icons.material.Resize
import ru.tech.imageresizershrinker.presentation.root.icons.material.Toolbox
import ru.tech.imageresizershrinker.presentation.root.icons.material.Transparency

@Parcelize
sealed class Screen(
    val id: Int,
    val icon: @RawValue ImageVector?,
    @StringRes val title: Int,
    @StringRes val subtitle: Int
) : Parcelable {
    data object Main : Screen(-1, null, 0, 0)

    class SingleEdit(val uri: Uri? = null) : Screen(
        id = 0,
        icon = Icons.Rounded.CreateAlt,
        title = R.string.single_edit,
        subtitle = R.string.single_edit_sub
    )

    class ResizeAndConvert(val uris: List<Uri>? = null) : Screen(
        id = 1,
        icon = Icons.Filled.Resize,
        title = R.string.resize_and_convert,
        subtitle = R.string.resize_and_convert_sub
    )

    class ResizeByBytes(val uris: List<Uri>? = null) : Screen(
        id = 2,
        icon = Icons.Filled.Interface,
        title = R.string.by_bytes_resize,
        subtitle = R.string.by_bytes_resize_sub
    )

    class Crop(val uri: Uri? = null) : Screen(
        id = 3,
        icon = Icons.Rounded.Crop,
        title = R.string.crop,
        subtitle = R.string.crop_sub
    )

    class Filter(val uris: List<Uri>? = null) : Screen(
        id = 4,
        icon = Icons.Rounded.PhotoFilter,
        title = R.string.filter,
        subtitle = R.string.filter_sub
    )

    class Draw(val uri: Uri? = null) : Screen(
        id = 5,
        icon = Icons.Rounded.Draw,
        title = R.string.draw,
        subtitle = R.string.draw_sub
    )

    class Cipher(val uri: Uri? = null) : Screen(
        id = 6,
        icon = Icons.Rounded.Security,
        title = R.string.cipher,
        subtitle = R.string.cipher_sub
    )

    class EraseBackground(val uri: Uri? = null) : Screen(
        id = 7,
        icon = Icons.Filled.Transparency,
        title = R.string.background_remover,
        subtitle = R.string.background_remover_sub
    )

    class ImagePreview(val uris: List<Uri>? = null) : Screen(
        id = 8,
        icon = Icons.Rounded.Photo,
        title = R.string.image_preview,
        subtitle = R.string.image_preview_sub
    )

    class ImageStitching(val uris: List<Uri>? = null) : Screen(
        id = 9,
        icon = Icons.Outlined.Puzzle,
        title = R.string.image_stitching,
        subtitle = R.string.image_stitching_sub
    )

    class LoadNetImage(val url: String = "") : Screen(
        id = 10,
        icon = Icons.Rounded.Public,
        title = R.string.load_image_from_net,
        subtitle = R.string.load_image_from_net_sub
    )

    class PickColorFromImage(val uri: Uri? = null) : Screen(
        id = 11,
        icon = Icons.Rounded.Colorize,
        title = R.string.pick_color,
        subtitle = R.string.pick_color_sub
    )

    class GeneratePalette(val uri: Uri? = null) : Screen(
        id = 12,
        icon = Icons.Rounded.PaletteSwatch,
        title = R.string.generate_palette,
        subtitle = R.string.palette_sub
    )

    class DeleteExif(val uris: List<Uri>? = null) : Screen(
        id = 13,
        icon = Icons.Rounded.FingerprintOff,
        title = R.string.delete_exif,
        subtitle = R.string.delete_exif_sub
    )

    class Compare(val uris: List<Uri>? = null) : Screen(
        id = 14,
        icon = Icons.Rounded.Compare,
        title = R.string.compare,
        subtitle = R.string.compare_sub
    )

    class LimitResize(val uris: List<Uri>? = null) : Screen(
        id = 15,
        icon = Icons.Outlined.Margin,
        title = R.string.limits_resize,
        subtitle = R.string.limits_resize_sub
    )

    companion object {
        val typedEntries by lazy {
            listOf(
                listOf(
                    SingleEdit(),
                    ResizeAndConvert(),
                    Crop(),
                    ResizeByBytes(),
                    LimitResize(),
                ) to Triple(
                    R.string.edit,
                    Icons.Rounded.PhotoSizeSelectLarge,
                    Icons.Outlined.PhotoSizeSelectSmall
                ),
                listOf(
                    Filter(),
                    Draw(),
                    EraseBackground(),
                    ImageStitching(),
                    Cipher(),
                ) to Triple(
                    R.string.create,
                    Icons.Filled.AutoAwesome,
                    Icons.Outlined.AutoAwesome
                ),
                listOf(
                    PickColorFromImage(),
                    Compare(),
                    ImagePreview(),
                    LoadNetImage(),
                    GeneratePalette(),
                    DeleteExif(),
                ) to Triple(
                    R.string.tools,
                    Icons.Rounded.Toolbox,
                    Icons.Outlined.Toolbox
                )
            )
        }
        val entries by lazy {
            listOf(
                SingleEdit(),
                ResizeAndConvert(),
                ResizeByBytes(),
                Crop(),
                Filter(),
                Draw(),
                Cipher(),
                EraseBackground(),
                ImagePreview(),
                ImageStitching(),
                LoadNetImage(),
                PickColorFromImage(),
                GeneratePalette(),
                DeleteExif(),
                Compare(),
                LimitResize()
            )
        }
    }
}