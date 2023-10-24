package ru.tech.imageresizershrinker.presentation.root.transformation.filter

import android.graphics.Bitmap
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.domain.image.filters.Filter
import ru.tech.imageresizershrinker.domain.image.filters.FilterParam


class UiWhiteBalanceFilter(
    override val value: Pair<Float, Float> = 5000.0f to 0.0f,
) : UiFilter<Pair<Float, Float>>(
    title = R.string.white_balance,
    value = value,
    paramsInfo = listOf(
        FilterParam(R.string.temperature, 1000f..10000f, 0),
        FilterParam(R.string.tint, -100f..100f, 2)
    )
), Filter.WhiteBalance<Bitmap>