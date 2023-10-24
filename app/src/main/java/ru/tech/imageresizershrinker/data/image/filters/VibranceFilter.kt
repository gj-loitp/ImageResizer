package ru.tech.imageresizershrinker.data.image.filters


import android.content.Context
import android.graphics.Bitmap
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVibranceFilter
import ru.tech.imageresizershrinker.domain.image.filters.Filter


class VibranceFilter(
    private val context: Context,
    override val value: Float = 0f,
) : GPUFilterTransformation(context), Filter.Vibrance<Bitmap> {
    override val cacheKey: String
        get() = (value to context).hashCode().toString()

    override fun createFilter(): GPUImageFilter = GPUImageVibranceFilter(value)
}