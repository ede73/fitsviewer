package fi.iki.ede.fits.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@Composable
fun FitsViewer(
    initialBytes: ByteArray?,
    imageName: String,
    onRequestOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var document by remember { mutableStateOf<FitsDocument?>(null) }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var status by remember { mutableStateOf("Choose a FITS file to begin") }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var centerX by remember { mutableFloatStateOf(0.5f) }
    var centerY by remember { mutableFloatStateOf(0.5f) }
    var brightness by remember { mutableFloatStateOf(1f) }
    var gamma by remember { mutableFloatStateOf(1.2f) }
    var algorithm by remember { mutableStateOf(StretchAlgorithm.RNC) }

    LaunchedEffect(initialBytes) {
        if (initialBytes == null) return@LaunchedEffect
        status = "Reading $imageName…"
        runCatching { withContext(Dispatchers.Default) { FitsDocument.parse(initialBytes) } }
            .onSuccess { document = it; zoom = 1f; centerX = 0.5f; centerY = 0.5f; status = "" }
            .onFailure { document = null; status = "Could not read FITS: ${it.message ?: it::class.simpleName}" }
    }

    LaunchedEffect(document, canvasSize, zoom, centerX, centerY, brightness, gamma, algorithm) {
        val source = document ?: return@LaunchedEffect
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return@LaunchedEffect
        val info = source.metadata
        val scale = min(canvasSize.width.toFloat() / info.width, canvasSize.height.toFloat() / info.height)
        val outputWidth = (info.width * scale).toInt().coerceIn(1, 1536)
        val outputHeight = (info.height * scale).toInt().coerceIn(1, 1536)
        val cropWidth = (info.width / zoom).toInt().coerceAtLeast(1)
        val cropHeight = (info.height / zoom).toInt().coerceAtLeast(1)
        val left = (centerX * info.width - cropWidth / 2f).toInt().coerceIn(0, max(0, info.width - cropWidth))
        val top = (centerY * info.height - cropHeight / 2f).toInt().coerceIn(0, max(0, info.height - cropHeight))
        val context = currentCoroutineContext()
        runCatching {
            val pixels = withContext(Dispatchers.Default) {
                source.render(left, top, cropWidth, cropHeight, outputWidth, outputHeight,
                    StretchSettings(brightness, gamma, algorithm = algorithm)) { !context.isActive }
            }
            if (pixels.isNotEmpty()) createPlatformImageBitmap(outputWidth, outputHeight, pixels) else null
        }.onSuccess { rendered ->
            if (context.isActive && rendered != null) bitmap = rendered
            if (context.isActive) status = ""
        }.onFailure { if (context.isActive) status = "Could not render image: ${it.message ?: it::class.simpleName}" }
    }

    MaterialTheme {
        Column(modifier.fillMaxSize().padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRequestOpen) { Text("Open FITS file…") }
                Text(imageName, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { zoom = (zoom / 2f).coerceAtLeast(1f); centerX = 0.5f; centerY = 0.5f }) { Text("−") }
                Text("${zoom}×", modifier = Modifier.weight(1f))
                Button(onClick = { zoom = (zoom * 2f).coerceAtMost(32f) }) { Text("+") }
                Button(onClick = { zoom = 1f; centerX = 0.5f; centerY = 0.5f }) { Text("Reset") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Brightness ${"%.1f".format(brightness)}", modifier = Modifier.width(120.dp))
                Slider(brightness, { brightness = it }, valueRange = 0.1f..40f, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gamma ${"%.2f".format(gamma)}", modifier = Modifier.width(120.dp))
                Slider(gamma, { gamma = it }, valueRange = 0.2f..3f, modifier = Modifier.weight(1f))
                Button(onClick = { algorithm = if (algorithm == StretchAlgorithm.RNC) StretchAlgorithm.LINEAR else StretchAlgorithm.RNC }) {
                    Text(algorithm.name)
                }
            }
            val info = document?.metadata
            if (info != null) Text("${info.width} × ${info.height} • BITPIX ${info.bitpix} • ${zoom}× • BSCALE ${info.bscale} / BZERO ${info.bzero}")
            if (status.isNotBlank()) Text(status)
            Canvas(
                Modifier.fillMaxWidth().weight(1f).border(1.dp, Color.DarkGray)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(document) {
                        detectTransformGestures { _, pan, pinch, _ ->
                            val nextZoom = (zoom * pinch).coerceIn(1f, 32f)
                            centerX = (centerX - pan.x / size.width / zoom).coerceIn(0.5f / nextZoom, 1f - 0.5f / nextZoom)
                            centerY = (centerY - pan.y / size.height / zoom).coerceIn(0.5f / nextZoom, 1f - 0.5f / nextZoom)
                            zoom = nextZoom
                        }
                    }
            ) {
                val image = bitmap ?: return@Canvas
                val fit = min(size.width / image.width, size.height / image.height)
                val width = (image.width * fit).toInt().coerceAtLeast(1)
                val height = (image.height * fit).toInt().coerceAtLeast(1)
                drawImage(image, dstOffset = IntOffset(((size.width - width) / 2).toInt(), ((size.height - height) / 2).toInt()),
                    dstSize = IntSize(width, height))
            }
        }
    }
}

internal expect fun createPlatformImageBitmap(width: Int, height: Int, argbPixels: IntArray): ImageBitmap
