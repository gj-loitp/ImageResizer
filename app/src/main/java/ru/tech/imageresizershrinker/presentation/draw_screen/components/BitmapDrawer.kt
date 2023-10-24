package ru.tech.imageresizershrinker.presentation.draw_screen.components

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.PorterDuff
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.BitmapCompat
import com.smarttoolfactory.gesture.MotionEvent
import com.smarttoolfactory.gesture.pointerMotionEvents
import com.smarttoolfactory.image.util.update
import com.smarttoolfactory.image.zoom.animatedZoom
import com.smarttoolfactory.image.zoom.rememberAnimatedZoomState
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.domain.image.ImageManager
import ru.tech.imageresizershrinker.domain.image.draw.DrawMode
import ru.tech.imageresizershrinker.domain.image.draw.Pt
import ru.tech.imageresizershrinker.domain.image.filters.Filter
import ru.tech.imageresizershrinker.domain.model.IntegerSize
import ru.tech.imageresizershrinker.presentation.root.model.UiPathPaint
import ru.tech.imageresizershrinker.presentation.root.theme.outlineVariant
import ru.tech.imageresizershrinker.presentation.root.transformation.filter.UiPixelationFilter
import ru.tech.imageresizershrinker.presentation.root.transformation.filter.UiStackBlurFilter
import ru.tech.imageresizershrinker.presentation.root.utils.helper.rotateVector
import ru.tech.imageresizershrinker.presentation.root.utils.helper.scaleToFitCanvas
import ru.tech.imageresizershrinker.presentation.root.widget.modifier.transparencyChecker
import kotlin.math.abs
import android.graphics.Canvas as AndroidCanvas


@Composable
fun BitmapDrawer(
    imageBitmap: ImageBitmap,
    imageManager: ImageManager<Bitmap, *>,
    paths: List<UiPathPaint>,
    brushSoftness: Pt,
    onAddPath: (UiPathPaint) -> Unit,
    strokeWidth: Pt,
    isEraserOn: Boolean,
    drawMode: DrawMode,
    modifier: Modifier,
    drawArrowsEnabled: Boolean,
    onDraw: (Bitmap) -> Unit,
    backgroundColor: Color,
    zoomEnabled: Boolean,
    drawColor: Color
) {
    val zoomState = rememberAnimatedZoomState(maxZoom = 30f)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (zoomEnabled) {
                    Modifier.animatedZoom(animatedZoomState = zoomState)
                } else {
                    Modifier.graphicsLayer {
                        update(zoomState)
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier) {
            var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
            // This is our motion event we get from touch motion
            var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
            // This is previous motion event before next touch is saved into this current position
            var previousPosition by remember { mutableStateOf(Offset.Unspecified) }

            val imageWidth = constraints.maxWidth
            val imageHeight = constraints.maxHeight

            val drawImageBitmap = remember(constraints, backgroundColor) {
                BitmapCompat.createScaledBitmap(
                    imageBitmap.asAndroidBitmap(),
                    imageWidth,
                    imageHeight,
                    null,
                    true
                ).apply {
                    val canvas = AndroidCanvas(this)
                    val paint = android.graphics.Paint().apply {
                        color = backgroundColor.toArgb()
                    }
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                }.asImageBitmap()
            }

            val drawBitmap: ImageBitmap = remember(constraints) {
                Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
                    .asImageBitmap()
            }

            val drawPathBitmap: ImageBitmap = remember(constraints) {
                Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
                    .asImageBitmap()
            }

            var invalidations by remember {
                mutableIntStateOf(0)
            }

            val outputImage by remember(invalidations) {
                derivedStateOf {
                    drawImageBitmap.overlay(drawBitmap)
                }
            }

            LaunchedEffect(invalidations) {
                onDraw(outputImage.overlay(drawPathBitmap).asAndroidBitmap())
            }

            val canvas: Canvas = remember(constraints) {
                Canvas(drawBitmap)
            }

            val drawPathCanvas: Canvas = remember(constraints) {
                Canvas(drawPathBitmap)
            }

            val canvasSize = remember(canvas.nativeCanvas) {
                IntegerSize(canvas.nativeCanvas.width, canvas.nativeCanvas.height)
            }

            fun transformationsForMode(
                drawMode: DrawMode
            ): List<Filter<Bitmap, *>> = when (drawMode) {
                is DrawMode.PathEffect.PrivacyBlur -> {
                    listOf(
                        UiStackBlurFilter(
                            value = when {
                                drawMode.blurRadius < 10 -> 0.8f
                                drawMode.blurRadius < 20 -> 0.5f
                                else -> 0.3f
                            } to drawMode.blurRadius
                        )
                    )
                }

                is DrawMode.PathEffect.Pixelation -> {
                    listOf(
                        UiStackBlurFilter(
                            value = when {
                                drawMode.pixelSize.value < 10 -> 0.8f
                                drawMode.pixelSize.value < 20 -> 0.5f
                                else -> 0.3f
                            } to 20
                        ),
                        UiPixelationFilter(
                            value = drawMode.pixelSize.toPx(canvasSize)
                        )
                    )
                }

                else -> emptyList()
            }

            val drawPaint =
                remember(strokeWidth, isEraserOn, drawColor, brushSoftness, drawMode, canvasSize) {
                    Paint().apply {
                        blendMode = if (!isEraserOn) blendMode else BlendMode.Clear
                        style = PaintingStyle.Stroke
                        strokeCap =
                            if (drawMode is DrawMode.Highlighter) StrokeCap.Square else StrokeCap.Round
                        color = if (drawMode is DrawMode.PathEffect) {
                            Color.Transparent
                        } else drawColor
                        alpha = drawColor.alpha
                        this.strokeWidth = strokeWidth.toPx(canvasSize)
                        strokeJoin = StrokeJoin.Round
                        isAntiAlias = true
                    }.asFrameworkPaint().apply {
                        if (drawMode is DrawMode.Neon && !isEraserOn) {
                            this.color = Color.White.toArgb()
                            setShadowLayer(
                                brushSoftness.toPx(canvasSize),
                                0f,
                                0f,
                                drawColor
                                    .copy(alpha = .8f)
                                    .toArgb()
                            )
                        } else if (brushSoftness.value > 0f) {
                            maskFilter = BlurMaskFilter(
                                brushSoftness.toPx(canvasSize),
                                BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    }
                }

            var drawPath by remember(
                drawMode,
                strokeWidth,
                isEraserOn,
                drawColor,
                brushSoftness
            ) { mutableStateOf(Path()) }

            LaunchedEffect(paths, drawMode, backgroundColor, constraints) {
                invalidations++
            }

            canvas.apply {
                when (motionEvent) {

                    MotionEvent.Down -> {
                        drawPath.moveTo(currentPosition.x, currentPosition.y)
                        previousPosition = currentPosition
                    }

                    MotionEvent.Move -> {
                        drawPath.quadraticBezierTo(
                            previousPosition.x,
                            previousPosition.y,
                            (previousPosition.x + currentPosition.x) / 2,
                            (previousPosition.y + currentPosition.y) / 2
                        )
                        previousPosition = currentPosition
                    }

                    MotionEvent.Up -> {
                        drawPath.lineTo(currentPosition.x, currentPosition.y)

                        if (drawArrowsEnabled && !isEraserOn) {
                            val preLastPoint = PathMeasure().apply {
                                setPath(drawPath, false)
                            }.let {
                                it.getPosition(it.length - strokeWidth.toPx(canvasSize) * 3f)
                            }.let { if (it.isUnspecified) Offset.Zero else it }

                            val lastPoint = currentPosition.let {
                                if (it.isUnspecified) Offset.Zero else it
                            }

                            val arrowVector = lastPoint - preLastPoint
                            fun drawArrow() {

                                val (rx1, ry1) = arrowVector.rotateVector(150.0)
                                val (rx2, ry2) = arrowVector.rotateVector(210.0)


                                drawPath.apply {
                                    relativeLineTo(rx1, ry1)
                                    moveTo(lastPoint.x, lastPoint.y)
                                    relativeLineTo(rx2, ry2)
                                }
                            }

                            if (abs(arrowVector.x) < 3f * strokeWidth.toPx(canvasSize) && abs(
                                    arrowVector.y
                                ) < 3f * strokeWidth.toPx(canvasSize) && preLastPoint != Offset.Zero
                            ) {
                                drawArrow()
                            }
                        }

                        currentPosition = Offset.Unspecified
                        previousPosition = currentPosition
                        motionEvent = MotionEvent.Idle
                        onAddPath(
                            UiPathPaint(
                                path = drawPath,
                                strokeWidth = strokeWidth,
                                brushSoftness = brushSoftness,
                                drawColor = drawColor,
                                isErasing = isEraserOn,
                                drawMode = drawMode,
                                canvasSize = canvasSize
                            )
                        )
                        scope.launch {
                            if (drawMode is DrawMode.PathEffect && !isEraserOn) Unit
                            else drawPath = Path()
                        }
                    }

                    else -> Unit
                }

                with(nativeCanvas) {
                    drawColor(Color.Transparent.toArgb(), PorterDuff.Mode.CLEAR)
                    drawColor(backgroundColor.toArgb())

                    paths.forEach { (nonScaledPath, nonScaledStroke, radius, drawColor, isErasing, effect, size) ->
                        val stroke = nonScaledStroke.toPx(canvasSize)
                        val path = nonScaledPath.scaleToFitCanvas(
                            currentSize = canvasSize,
                            oldSize = size
                        )
                        if (effect is DrawMode.PathEffect && !isErasing) {
                            var shaderSource by remember(backgroundColor) {
                                mutableStateOf<ImageBitmap?>(null)
                            }
                            LaunchedEffect(shaderSource, invalidations) {
                                if (shaderSource == null || invalidations <= paths.size) {
                                    shaderSource = imageManager.filter(
                                        image = drawImageBitmap.overlay(drawBitmap)
                                            .asAndroidBitmap(),
                                        filters = transformationsForMode(effect)
                                    )?.asImageBitmap()?.clipBitmap(
                                        path = path,
                                        paint = Paint().apply {
                                            style = PaintingStyle.Stroke
                                            strokeCap = StrokeCap.Round
                                            this.strokeWidth = stroke
                                            strokeJoin = StrokeJoin.Round
                                            isAntiAlias = true
                                            color = Color.Transparent
                                            blendMode = BlendMode.Clear
                                        }
                                    )?.also {
                                        it.prepareToDraw()
                                        invalidations++
                                    }
                                }
                            }
                            if (shaderSource != null) {
                                LaunchedEffect(shaderSource) {
                                    drawPath = Path()
                                }
                                drawImage(
                                    image = shaderSource!!,
                                    topLeftOffset = Offset.Zero,
                                    paint = Paint()
                                )
                            }
                        } else {
                            drawPath(
                                path.asAndroidPath(),
                                Paint().apply {
                                    blendMode = if (!isErasing) blendMode else BlendMode.Clear
                                    style = PaintingStyle.Stroke
                                    strokeCap =
                                        if (effect is DrawMode.Highlighter) StrokeCap.Square else StrokeCap.Round
                                    this.strokeWidth = stroke
                                    strokeJoin = StrokeJoin.Round
                                    isAntiAlias = true
                                    color = drawColor
                                    alpha = drawColor.alpha
                                }.asFrameworkPaint().apply {
                                    if (effect is DrawMode.Neon && !isErasing) {
                                        this.color = Color.White.toArgb()
                                        setShadowLayer(
                                            radius.toPx(canvasSize),
                                            0f,
                                            0f,
                                            drawColor
                                                .copy(alpha = .8f)
                                                .toArgb()
                                        )
                                    } else if (radius.value > 0f) {
                                        maskFilter =
                                            BlurMaskFilter(
                                                radius.toPx(canvasSize),
                                                BlurMaskFilter.Blur.NORMAL
                                            )
                                    }
                                }
                            )
                        }
                    }

                    if (drawMode !is DrawMode.PathEffect || isEraserOn) {
                        drawPath(
                            drawPath.asAndroidPath(),
                            drawPaint
                        )
                    }
                }
            }

            var pathEffectBitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }

            LaunchedEffect(outputImage, paths, backgroundColor, drawMode) {
                pathEffectBitmap = imageManager.filter(
                    image = outputImage.asAndroidBitmap(),
                    filters = transformationsForMode(drawMode)
                )?.asImageBitmap()
            }

            val shaderBitmap = remember(pathEffectBitmap) {
                pathEffectBitmap?.asAndroidBitmap()?.let {
                    BitmapCompat.createScaledBitmap(
                        it,
                        imageWidth,
                        imageHeight,
                        null,
                        true
                    ).asImageBitmap()
                }
            }

            if (drawMode is DrawMode.PathEffect && shaderBitmap != null && !isEraserOn) {
                drawPathCanvas.apply {
                    with(nativeCanvas) {
                        drawColor(Color.Transparent.toArgb(), PorterDuff.Mode.CLEAR)

                        val paint = Paint().apply {
                            style = PaintingStyle.Stroke
                            strokeCap = StrokeCap.Round
                            this.strokeWidth = strokeWidth.toPx(canvasSize)
                            strokeJoin = StrokeJoin.Round
                            isAntiAlias = true
                            color = Color.Transparent
                            blendMode = BlendMode.Clear
                        }
                        val newPath = android.graphics.Path(drawPath.asAndroidPath())

                        drawImage(
                            shaderBitmap, Offset.Zero, Paint()
                        )
                        drawPath(
                            newPath.apply {
                                fillType = android.graphics.Path.FillType.INVERSE_WINDING
                            },
                            paint.asFrameworkPaint()
                        )
                    }
                }
            }

            val canvasModifier = Modifier.pointerMotionEvents(
                onDown = { pointerInputChange ->
                    motionEvent = MotionEvent.Down
                    currentPosition = pointerInputChange.position
                    pointerInputChange.consume()
                    invalidations++
                },
                onMove = { pointerInputChange ->
                    motionEvent = MotionEvent.Move
                    currentPosition = pointerInputChange.position
                    pointerInputChange.consume()
                    invalidations++
                },
                onUp = { pointerInputChange ->
                    motionEvent = MotionEvent.Up
                    pointerInputChange.consume()
                    invalidations++
                },
                delayAfterDownInMillis = 20
            )

            Image(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (!zoomEnabled) canvasModifier
                        else Modifier
                    )
                    .clip(RoundedCornerShape(2.dp))
                    .transparencyChecker()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant(),
                        RoundedCornerShape(2.dp)
                    ),
                bitmap = remember(invalidations) {
                    derivedStateOf {
                        outputImage.overlay(drawPathBitmap)
                    }
                }.value,
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

private fun ImageBitmap.clipBitmap(
    path: Path,
    paint: Paint,
): ImageBitmap {
    val bitmap = this.asAndroidBitmap()
    val newPath = android.graphics.Path(path.asAndroidPath())
    AndroidCanvas(bitmap).apply {
        drawPath(
            newPath.apply {
                fillType = android.graphics.Path.FillType.INVERSE_WINDING
            },
            paint.asFrameworkPaint()
        )
    }
    return bitmap.asImageBitmap()
}

private fun ImageBitmap.overlay(overlay: ImageBitmap): ImageBitmap {
    val image = this.asAndroidBitmap()
    val finalBitmap = Bitmap.createBitmap(image.width, image.height, image.config)
    val canvas = AndroidCanvas(finalBitmap)
    canvas.drawBitmap(image, Matrix(), null)
    canvas.drawBitmap(overlay.asAndroidBitmap(), 0f, 0f, null)
    return finalBitmap.asImageBitmap()
}