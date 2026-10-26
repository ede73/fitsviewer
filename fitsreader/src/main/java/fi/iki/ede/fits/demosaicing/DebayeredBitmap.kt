package fi.iki.ede.fits.demosaicing

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import fi.iki.ede.fits.fits.RawFITSRasterBitmap
import fi.iki.ede.fits.fits.datadefs.FITSInfo
import fi.iki.ede.fits.fits.datadefs.RGBColor
import kotlin.math.max
import kotlin.math.min

private const val TAG = "DebayeredBitmap"

/** Pixel access and bounded rendering over the original FITS samples. No expanded RGB image is cached. */
class DebayeredBitmap(private val raw: RawFITSRasterBitmap) {
    val fitsInfo: FITSInfo = raw.fitsInfo
    val rowSizeOctets: Int = raw.rowSizeOctets
    val rRangeOriginal = ColorStats()
    val gRangeOriginal = ColorStats()
    val bRangeOriginal = ColorStats()
    val rRangeScaled = ColorStats()
    val gRangeScaled = ColorStats()
    val bRangeScaled = ColorStats()
    val histogramOriginal: Histogram
    var histogramScaled: Histogram? = null

    init {
        // Sample statistics for the overview; source pixels remain untouched and uncached.
        val bins = IntArray(256)
        var lo = Float.MAX_VALUE
        var hi = -Float.MAX_VALUE
        val longestSide = max(fitsInfo.width, fitsInfo.height)
        val statsStep = max(1, (longestSide + 511) / 512)
        for (y in 0 until fitsInfo.height step statsStep) for (x in 0 until fitsInfo.width step statsStep) {
            val (r, g, b) = getDebayeredPixel(x, y)
            rRangeOriginal.addValue(r); gRangeOriginal.addValue(g); bRangeOriginal.addValue(b)
            lo = min(lo, min(r, min(g, b))); hi = max(hi, max(r, max(g, b)))
        }
        val range = (hi - lo).takeIf { it > 0f } ?: 1f
        for (y in 0 until fitsInfo.height step statsStep) for (x in 0 until fitsInfo.width step statsStep) {
            val (r, g, b) = getDebayeredPixel(x, y)
            val lum = (0.2126f * r + 0.7152f * g + 0.0722f * b - lo) * 255f / range
            bins[lum.toInt().coerceIn(0, 255)]++
        }
        histogramOriginal = Histogram(bins)
    }

    private fun sample(x: Int, y: Int): Pair<Float, RGBColor> {
        val cx = x.coerceIn(0, fitsInfo.width - 1)
        val cy = y.coerceIn(0, fitsInfo.height - 1)
        return raw.pixelReader.getPixel(cx, cy)
    }

    fun getDebayeredPixel(x: Int, y: Int): Triple<Float, Float, Float> {
        require(x in 0 until fitsInfo.width && y in 0 until fitsInfo.height)
        if (fitsInfo.naxis3 == 3) {
            val h = fitsInfo.height
            return Triple(raw.pixelReader.getPixel(x, y).first,
                raw.pixelReader.getPixel(x, y + h).first,
                raw.pixelReader.getPixel(x, y + 2 * h).first)
        }
        val (value, color) = sample(x, y)
        if (color == RGBColor.BW) return Triple(value, value, value)
        val sums = FloatArray(3)
        val counts = IntArray(3)
        for (dy in -1..1) for (dx in -1..1) {
            if (dx == 0 && dy == 0) continue
            val nx = x + dx; val ny = y + dy
            // Keep FITS' flat raster edge behavior: a negative x at y>0 can still address
            // the preceding row's final sample in the historic parser.
            val byteIndex = ny.toLong() * raw.rowSizeOctets + nx.toLong() * raw.pixelReader.octetsPerPixel
            if (byteIndex < 0 || byteIndex + raw.pixelReader.octetsPerPixel > raw.rawFITSBitmap.size) continue
            val (neighbor, neighborColor) = raw.pixelReader.getPixel(nx, ny)
            if (neighborColor == color) continue
            val idx = when (neighborColor) { RGBColor.R -> 0; RGBColor.G -> 1; RGBColor.B -> 2; RGBColor.BW -> -1 }
            if (idx >= 0) { sums[idx] += neighbor; counts[idx]++ }
        }
        val own = when (color) { RGBColor.R -> 0; RGBColor.G -> 1; RGBColor.B -> 2; RGBColor.BW -> -1 }
        if (own >= 0) { sums[own] = value; counts[own] = 1 }
        for (i in 0..2) if (counts[i] > 0) sums[i] /= counts[i]
        return Triple(sums[0], sums[1], sums[2])
    }

    /** Render a source rectangle to a bounded bitmap, sampling from original FITS pixels. */
    fun makeBitmap(
        sourceLeft: Int, sourceTop: Int, sourceWidth: Int, sourceHeight: Int,
        outputWidth: Int, outputHeight: Int, brightnessScaling: Float,
        contrastStretch: (colorNBits: Int, bitDepth: Int) -> Int,
        isCancelled: () -> Boolean
    ): Bitmap? {
        require(sourceWidth > 0 && sourceHeight > 0 && outputWidth > 0 && outputHeight > 0)
        val left = sourceLeft.coerceIn(0, fitsInfo.width - 1)
        val top = sourceTop.coerceIn(0, fitsInfo.height - 1)
        val right = (sourceLeft.toLong() + sourceWidth).coerceIn((left + 1).toLong(), fitsInfo.width.toLong()).toInt()
        val bottom = (sourceTop.toLong() + sourceHeight).coerceIn((top + 1).toLong(), fitsInfo.height.toLong()).toInt()
        val actualWidth = right - left
        val actualHeight = bottom - top
        val pixels = IntArray(Math.multiplyExact(outputWidth, outputHeight))
        val bitDepth = raw.pixelBitSpecification.getBitDepth()
        for (oy in 0 until outputHeight) {
            if (isCancelled()) return null
            val sy = top + ((oy + 0.5f) * actualHeight / outputHeight).toInt().coerceIn(0, actualHeight - 1)
            for (ox in 0 until outputWidth) {
                val sx = left + ((ox + 0.5f) * actualWidth / outputWidth).toInt().coerceIn(0, actualWidth - 1)
                val (r0, g0, b0) = getDebayeredPixel(sx, sy)
                val scale = brightnessScaling.coerceAtLeast(0.0001f)
                val maxInput = ((1L shl bitDepth) - 1).toInt()
                fun toDisplayChannel(value: Float): Int {
                    val stretched = contrastStretch((value * scale).toInt().coerceIn(0, maxInput), bitDepth)
                    return (stretched.toDouble() * 255.0 / maxInput).toInt().coerceIn(0, 255)
                }
                val r = toDisplayChannel(r0)
                val g = toDisplayChannel(g0)
                val b = toDisplayChannel(b0)
                pixels[oy * outputWidth + ox] = Color.rgb(r, g, b)
            }
        }
        val bins = IntArray(256)
        pixels.forEach { bins[(0.2126 * Color.red(it) + 0.7152 * Color.green(it) + 0.0722 * Color.blue(it)).toInt().coerceIn(0, 255)]++ }
        histogramScaled = Histogram(bins)
        return Bitmap.createBitmap(pixels, outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
    }

    /** Compatibility helper for existing callers; dimensions are clamped to the source image. */
    fun makeBitmap(brightnessScaling: Float, sizeDownscale: Int,
        contrastStretch: (colorNBits: Int, bitDepth: Int) -> Int, isCancelled: () -> Boolean): Bitmap? {
        val step = sizeDownscale.coerceAtLeast(1)
        return makeBitmap(0, 0, fitsInfo.width, fitsInfo.height,
            (fitsInfo.width / step).coerceAtLeast(1), (fitsInfo.height / step).coerceAtLeast(1),
            brightnessScaling, contrastStretch, isCancelled)
    }

    override fun toString() = "${fitsInfo.width}x${fitsInfo.height} raw FITS view"
}
