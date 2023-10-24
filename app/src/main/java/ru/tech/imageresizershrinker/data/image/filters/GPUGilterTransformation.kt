package ru.tech.imageresizershrinker.data.image.filters

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.BitmapCompat
import coil.size.Size
import coil.size.pxOrElse
import coil.transform.Transformation
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import java.lang.Integer.max

abstract class GPUFilterTransformation(
    private val context: Context
) : Transformation, ru.tech.imageresizershrinker.domain.image.Transformation<Bitmap> {

    /**
     * Create the [GPUImageFilter] to apply to this [Transformation]
     */
    abstract fun createFilter(): GPUImageFilter

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val gpuImage = GPUImage(context)
        gpuImage.setImage(
            flexibleResize(
                image = input,
                max = max(
                    size.height.pxOrElse { input.height },
                    size.width.pxOrElse { input.width }
                )
            )
        )
        gpuImage.setFilter(createFilter())
        return gpuImage.bitmapWithFilterApplied
    }
}

private fun flexibleResize(image: Bitmap, max: Int): Bitmap {
    return runCatching {
        if (image.height >= image.width) {
            val aspectRatio = image.width.toDouble() / image.height.toDouble()
            val targetWidth = (max * aspectRatio).toInt()
            BitmapCompat.createScaledBitmap(image, targetWidth, max, null, true)
        } else {
            val aspectRatio = image.height.toDouble() / image.width.toDouble()
            val targetHeight = (max * aspectRatio).toInt()
            BitmapCompat.createScaledBitmap(image, max, targetHeight, null, true)
        }
    }.getOrNull() ?: image
}