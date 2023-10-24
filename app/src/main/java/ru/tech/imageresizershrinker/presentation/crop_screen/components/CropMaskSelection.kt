package ru.tech.imageresizershrinker.presentation.crop_screen.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.cropper.model.CornerRadiusProperties
import com.smarttoolfactory.cropper.model.CutCornerCropShape
import com.smarttoolfactory.cropper.model.ImageMaskOutline
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RoundedCornerCropShape
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import com.smarttoolfactory.cropper.widget.CropFrameDisplayCard
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.presentation.root.theme.outlineVariant
import ru.tech.imageresizershrinker.presentation.root.utils.helper.Picker
import ru.tech.imageresizershrinker.presentation.root.utils.helper.localImagePickerMode
import ru.tech.imageresizershrinker.presentation.root.utils.helper.rememberImagePicker
import ru.tech.imageresizershrinker.presentation.root.widget.controls.EnhancedSliderItem
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.container
import kotlin.math.roundToInt

@Composable
fun CropMaskSelection(
    modifier: Modifier = Modifier,
    selectedItem: CropOutlineProperty,
    loadImage: suspend (Uri) -> ImageBitmap?,
    onCropMaskChange: (CropOutlineProperty) -> Unit
) {
    var cornerRadius by rememberSaveable { mutableIntStateOf(20) }

    val outlineProperties = outlineProperties()

    val scope = rememberCoroutineScope()

    val maskLauncher =
        rememberImagePicker(
            mode = localImagePickerMode(Picker.Single)
        ) { uris ->
            uris.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                scope.launch {
                    loadImage(it)?.let {
                        onCropMaskChange(
                            outlineProperties.last().run {
                                copy(
                                    cropOutline = (cropOutline as ImageMaskOutline).copy(image = it)
                                )
                            }
                        )
                    }
                }
            }
        }


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.crop_mask),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 16.dp),
            fontWeight = FontWeight.Medium
        )
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 16.dp + WindowInsets
                    .navigationBars
                    .asPaddingValues()
                    .calculateEndPadding(LocalLayoutDirection.current)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(outlineProperties) { _, item ->
                val selected = selectedItem.cropOutline.id == item.cropOutline.id
                CropFrameDisplayCard(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .height(100.dp)
                        .container(
                            resultPadding = 0.dp,
                            color = animateColorAsState(
                                targetValue = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else MaterialTheme.colorScheme.surfaceContainerLowest,
                            ).value,
                            borderColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                0.7f
                            )
                            else MaterialTheme.colorScheme.outlineVariant()
                        )
                        .clickable {
                            if (item.cropOutline is ImageMaskOutline) {
                                maskLauncher.pickImage()
                            } else {
                                onCropMaskChange(item)
                            }
                            cornerRadius = 20
                        }
                        .padding(16.dp),
                    editable = false,
                    scale = 1f,
                    outlineColor = MaterialTheme.colorScheme.secondary,
                    title = "",
                    cropOutline = item.cropOutline
                )
            }
        }

        EnhancedSliderItem(
            visible = selectedItem.cropOutline.id == 1 || selectedItem.cropOutline.id == 2,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            value = cornerRadius,
            title = stringResource(R.string.radius),
            icon = null,
            onValueChange = {
                cornerRadius = it.roundToInt()
                if (selectedItem.cropOutline is CutCornerCropShape) {
                    onCropMaskChange(
                        selectedItem.copy(
                            cropOutline = CutCornerCropShape(
                                id = selectedItem.cropOutline.id,
                                title = selectedItem.cropOutline.title,
                                cornerRadius = CornerRadiusProperties(
                                    topStartPercent = cornerRadius,
                                    topEndPercent = cornerRadius,
                                    bottomStartPercent = cornerRadius,
                                    bottomEndPercent = cornerRadius
                                )
                            )
                        )
                    )
                } else if (selectedItem.cropOutline is RoundedCornerCropShape) {
                    onCropMaskChange(
                        selectedItem.copy(
                            cropOutline = RoundedCornerCropShape(
                                id = selectedItem.cropOutline.id,
                                title = selectedItem.cropOutline.title,
                                cornerRadius = CornerRadiusProperties(
                                    topStartPercent = cornerRadius,
                                    topEndPercent = cornerRadius,
                                    bottomStartPercent = cornerRadius,
                                    bottomEndPercent = cornerRadius
                                )
                            )
                        )
                    )
                }
            },
            valueRange = 0f..50f,
            steps = 50
        )
        AnimatedVisibility(selectedItem.cropOutline.title == OutlineType.ImageMask.name) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .container(shape = RoundedCornerShape(24.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.image_crop_mask_sub),
                    textAlign = TextAlign.Center,
                    color = LocalContentColor.current.copy(0.5f),
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}