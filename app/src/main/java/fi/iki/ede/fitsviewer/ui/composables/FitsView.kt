package fi.iki.ede.fitsviewer.ui.composables

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import fi.iki.ede.fits.fits.FITSLoader
import fi.iki.ede.fitsviewer.imagefuncsupport.ContrastAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private const val TAG = "Fits"

@Composable
fun FitsView(
    @RawRes todoReplaceWithSomethingElseFitsID: Int,
    brightnessScaling: Float,
    sizeDownscale: Int,
    stretch: MutableState<ContrastAlgorithm>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fitsLoader by remember { mutableStateOf<FITSLoader?>(null) }
    var msg by remember { mutableStateOf("") }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var centerX by remember { mutableFloatStateOf(0.5f) }
    var centerY by remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(todoReplaceWithSomethingElseFitsID) {
        msg = "Loading FITS…"
        val result = withContext(Dispatchers.IO) {
            runCatching { FITSLoader().apply { loadFITS(context, todoReplaceWithSomethingElseFitsID) } }
        }
        fitsLoader = result.getOrNull()
        zoom = 1f; centerX = 0.5f; centerY = 0.5f
        msg = result.exceptionOrNull()?.let { "Could not load FITS: ${it.message ?: it.javaClass.simpleName}" } ?: ""
    }

    LaunchedEffect(fitsLoader, canvasSize, zoom, centerX, centerY, brightnessScaling, stretch.value, sizeDownscale) {
        val loader = fitsLoader ?: return@LaunchedEffect
        val size = canvasSize
        if (size.width <= 0 || size.height <= 0) return@LaunchedEffect
        val raster = loader.raster ?: return@LaunchedEffect
        val image = raster.getBitmapSupport()
        val imageWidth = image.fitsInfo.width
        val imageHeight = image.fitsInfo.height
        val fitScale = min(size.width.toFloat() / imageWidth, size.height.toFloat() / imageHeight)
        val displayWidth = max(1, (imageWidth * fitScale).toInt())
        val displayHeight = max(1, (imageHeight * fitScale).toInt())
        val visibleWidth = min(imageWidth.toFloat(), imageWidth / zoom)
        val visibleHeight = min(imageHeight.toFloat(), imageHeight / zoom)
        val left = ((centerX * imageWidth) - visibleWidth / 2).toInt().coerceIn(0, max(0, imageWidth - visibleWidth.toInt()))
        val top = ((centerY * imageHeight) - visibleHeight / 2).toInt().coerceIn(0, max(0, imageHeight - visibleHeight.toInt()))
        val downscale = sizeDownscale.coerceAtLeast(1)
        val outputWidth = (displayWidth / downscale).coerceIn(1, 2048)
        val outputHeight = (displayHeight / downscale).coerceIn(1, 2048)
        var rendered: Bitmap? = null
        msg = "Rendering ${zoom.toInt().coerceAtLeast(1)}× view from FITS pixels…"
        val renderContext = currentCoroutineContext()
        try {
            val elapsed = measureTime {
                rendered = withContext(Dispatchers.Default) {
                    image.makeBitmap(left, top, visibleWidth.toInt().coerceAtLeast(1), visibleHeight.toInt().coerceAtLeast(1),
                        outputWidth, outputHeight, brightnessScaling, stretch.value) {
                        !renderContext.isActive
                    }
                }
            }
            val current = rendered
            if (!isActive) current?.recycle() else {
                bitmap?.recycle()
                bitmap = current
            }
            Log.d(TAG, "FITS crop rendered in ${elapsed.toInt(DurationUnit.MILLISECONDS)} ms")
            msg = ""
        } catch (error: Exception) {
            if (isActive) msg = "Could not render FITS view: ${error.message ?: error.javaClass.simpleName}"
        }
    }

    DisposableEffect(Unit) { onDispose { bitmap?.recycle() } }

    Column(modifier = modifier) {
        Text(msg)
        val current = bitmap
        if (current != null && fitsLoader?.raster != null) {
            Histogram(fitsLoader!!.raster!!.getBitmapSupport())
            Row {
                Text("${fitsLoader!!.raster!!.fitsInfo.width} × ${fitsLoader!!.raster!!.fitsInfo.height} FITS  •  ${"%.1f".format(zoom)}×")
                TextButton(onClick = { zoom = (zoom / 2f).coerceAtLeast(1f); centerX = 0.5f; centerY = 0.5f }) { Text("−") }
                TextButton(onClick = { zoom = (zoom * 2f).coerceAtMost(32f) }) { Text("+") }
                TextButton(onClick = { zoom = 1f; centerX = 0.5f; centerY = 0.5f }) { Text("Reset") }
            }
            Text("BScale:${fitsLoader!!.raster!!.fitsInfo.pixelLimits.bScale} BZero:${fitsLoader!!.raster!!.fitsInfo.pixelLimits.bZero}  BPP:${fitsLoader!!.raster!!.fitsInfo.pixelLimits.pixelResolution.getBitDepth()}")
        }
        Canvas(
            modifier = Modifier.fillMaxWidth().weight(1f).border(2.dp, Color.DarkGray)
                .onSizeChanged { canvasSize = it }
                .pointerInput(fitsLoader) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        val nextZoom = (zoom * gestureZoom).coerceIn(1f, 32f)
                        val dx = pan.x / size.width * (1f / zoom)
                        val dy = pan.y / size.height * (1f / zoom)
                        zoom = nextZoom
                        centerX = (centerX - dx).coerceIn(0.5f / nextZoom, 1f - 0.5f / nextZoom)
                        centerY = (centerY - dy).coerceIn(0.5f / nextZoom, 1f - 0.5f / nextZoom)
                    }
                }
        ) {
            val img = bitmap?.asImageBitmap() ?: return@Canvas
            val scale = min(size.width / img.width, size.height / img.height)
            val w = img.width * scale
            val h = img.height * scale
            drawImage(img, dstOffset = androidx.compose.ui.unit.IntOffset(((size.width - w) / 2).toInt(), ((size.height - h) / 2).toInt()),
                dstSize = IntSize(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1)))
        }
    }
}
