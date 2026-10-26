package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fi.iki.ede.fits.demosaicing.DebayeredBitmap

@Composable
fun Histogram(rasterBitmap: DebayeredBitmap, modifier: Modifier = Modifier) {
    Box(modifier = modifier.border(1.dp, Color.Magenta)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            val originalHistogram = rasterBitmap.histogramOriginal.histogram
            val barWidth = size.width / originalHistogram.size
            val maxCount = rasterBitmap.histogramOriginal.maxCount
            originalHistogram.forEachIndexed { index, count ->
                val barHeight =
                    (size.height * (count.toFloat() / maxCount)).coerceIn(0f, size.height)
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(index * barWidth, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
    if (rasterBitmap.histogramScaled != null) {
        Box(modifier = modifier.border(1.dp, Color.Magenta)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                val scaledHistogram = rasterBitmap.histogramScaled!!.histogram
                val barWidth = size.width / scaledHistogram.size
                val maxCount = rasterBitmap.histogramScaled!!.maxCount
                scaledHistogram.forEachIndexed { index, count ->
                    val barHeight =
                        (size.height * (count.toFloat() / maxCount)).coerceIn(0f, size.height)
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(index * barWidth, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
        }
    }
}
