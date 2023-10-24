package ru.tech.imageresizershrinker.presentation.root.transformation.filter

import android.graphics.Bitmap
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.domain.image.filters.Filter
import ru.tech.imageresizershrinker.domain.image.filters.FilterParam


class UiKuwaharaFilter(
    override val value: Float = 3f,
) : UiFilter<Float>(
    title = R.string.kuwahara,
    value = value,
    paramsInfo = listOf(
        FilterParam(null, 0f..10f, 0)
    )
), Filter.Kuwahara<Bitmap>