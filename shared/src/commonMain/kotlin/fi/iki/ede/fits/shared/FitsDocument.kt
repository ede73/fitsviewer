package fi.iki.ede.fits.shared

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class FitsMetadata(
    val width: Int,
    val height: Int,
    val bitpix: Int,
    val planes: Int,
    val bscale: Double,
    val bzero: Double,
    val bayerPattern: String?
)

data class StretchSettings(
    val brightness: Float = 1f,
    val gamma: Float = 1.46f,
    val low: Float = 0f,
    val high: Float = 0.8f,
    val algorithm: StretchAlgorithm = StretchAlgorithm.RNC
)

enum class StretchAlgorithm { RNC, LINEAR }

/** Platform-neutral FITS image backed by the original on-disk samples. */
class FitsDocument private constructor(
    private val fileBytes: ByteArray,
    private val dataOffset: Int,
    val metadata: FitsMetadata
) {
    private val bytesPerPixel = metadata.bitpix.let { if (it < 0) -it else it } / 8
    private val planePixels = metadata.width * metadata.height
    private val floatingRange: Pair<Double, Double>? = if (metadata.bitpix < 0) {
        var low = Double.POSITIVE_INFINITY
        var high = Double.NEGATIVE_INFINITY
        for (plane in 0 until metadata.planes) for (y in 0 until metadata.height) for (x in 0 until metadata.width) {
            val value = pixel(x, y, plane)
            if (value.isFinite()) { low = min(low, value); high = max(high, value) }
        }
        if (low.isFinite() && high.isFinite() && high > low) low to high else 0.0 to 1.0
    } else null

    fun pixel(x: Int, y: Int, plane: Int = 0): Double {
        require(x in 0 until metadata.width && y in 0 until metadata.height)
        require(plane in 0 until metadata.planes)
        val index = dataOffset + (plane * planePixels + y * metadata.width + x) * bytesPerPixel
        var bits = 0L
        for (i in 0 until bytesPerPixel) bits = (bits shl 8) or (fileBytes[index + i].toLong() and 0xffL)
        val value = when (metadata.bitpix) {
            8 -> (bits and 0xffL).toDouble()
            16 -> bits.toShort().toDouble()
            32 -> bits.toInt().toDouble()
            64 -> bits.toDouble()
            -32 -> Float.fromBits(bits.toInt()).toDouble()
            -64 -> Double.fromBits(bits)
            else -> error("Unsupported BITPIX ${metadata.bitpix}")
        }
        return if (metadata.bitpix < 0) value else value * metadata.bscale + metadata.bzero
    }

    fun rgb(x: Int, y: Int): Triple<Double, Double, Double> {
        if (metadata.planes >= 3) return Triple(pixel(x, y, 0), pixel(x, y, 1), pixel(x, y, 2))
        val pattern = metadata.bayerPattern
        if (pattern.isNullOrBlank()) {
            val value = pixel(x, y)
            return Triple(value, value, value)
        }
        val own = bayerColor(pattern, x, y)
        val samples = DoubleArray(3)
        val counts = IntArray(3)
        for (dy in -1..1) for (dx in -1..1) {
            val nx = x + dx; val ny = y + dy
            if ((dx == 0 && dy == 0) || nx !in 0 until metadata.width || ny !in 0 until metadata.height) continue
            val color = bayerColor(pattern, nx, ny)
            val index = when (color) { 'R' -> 0; 'G' -> 1; 'B' -> 2; else -> -1 }
            if (index >= 0) { samples[index] += pixel(nx, ny); counts[index]++ }
        }
        val ownIndex = when (own) { 'R' -> 0; 'G' -> 1; else -> 2 }
        samples[ownIndex] = pixel(x, y); counts[ownIndex] = 1
        for (i in samples.indices) if (counts[i] > 0) samples[i] /= counts[i]
        return Triple(samples[0], samples[1], samples[2])
    }

    /** ARGB pixels rendered from a crop of the untouched FITS source data. */
    fun render(
        left: Int, top: Int, cropWidth: Int, cropHeight: Int,
        outputWidth: Int, outputHeight: Int,
        settings: StretchSettings,
        cancelled: () -> Boolean = { false }
    ): IntArray {
        require(cropWidth > 0 && cropHeight > 0 && outputWidth > 0 && outputHeight > 0)
        require(outputWidth.toLong() * outputHeight <= MAX_RENDER_PIXELS) { "Rendered view is too large" }
        val x0 = left.coerceIn(0, metadata.width - 1)
        val y0 = top.coerceIn(0, metadata.height - 1)
        val w = cropWidth.coerceAtMost(metadata.width - x0)
        val h = cropHeight.coerceAtMost(metadata.height - y0)
        val output = IntArray(outputWidth * outputHeight)
        val maximum = when (metadata.bitpix) {
            8 -> 255.0
            16 -> 65535.0
            32 -> Int.MAX_VALUE.toDouble()
            64 -> Long.MAX_VALUE.toDouble()
            else -> 1.0
        }
        val floatLow = floatingRange?.first ?: 0.0
        val floatSpan = floatingRange?.let { (it.second - it.first).coerceAtLeast(Double.MIN_VALUE) } ?: 1.0
        for (oy in 0 until outputHeight) {
            if (cancelled()) return intArrayOf()
            val sy = y0 + ((oy + 0.5) * h / outputHeight).toInt().coerceIn(0, h - 1)
            for (ox in 0 until outputWidth) {
                val sx = x0 + ((ox + 0.5) * w / outputWidth).toInt().coerceIn(0, w - 1)
                val (r, g, b) = rgb(sx, sy)
                val rr = displayChannel(if (floatingRange != null) (r - floatLow) / floatSpan else r, maximum, settings)
                val gg = displayChannel(if (floatingRange != null) (g - floatLow) / floatSpan else g, maximum, settings)
                val bb = displayChannel(if (floatingRange != null) (b - floatLow) / floatSpan else b, maximum, settings)
                output[oy * outputWidth + ox] = (0xff shl 24) or (rr shl 16) or (gg shl 8) or bb
            }
        }
        return output
    }

    private fun displayChannel(sample: Double, maximum: Double, settings: StretchSettings): Int {
        val input = (sample * settings.brightness).coerceIn(0.0, maximum)
        val normalized = (input / maximum).toFloat().coerceIn(0f, 1f)
        val stretched = when (settings.algorithm) {
            StretchAlgorithm.LINEAR -> ((normalized - settings.low) / (settings.high - settings.low).coerceAtLeast(0.0001f)).coerceIn(0f, 1f)
            StretchAlgorithm.RNC -> {
                val gamma = normalized.toDouble().pow(1.0 / settings.gamma.coerceAtLeast(0.01f)).toFloat()
                ((gamma - settings.low) / (settings.high - settings.low).coerceAtLeast(0.0001f)).coerceIn(0f, 1f)
            }
        }
        return (stretched * 255f).toInt().coerceIn(0, 255)
    }

    private fun bayerColor(pattern: String, x: Int, y: Int): Char {
        val normalized = pattern.uppercase()
        return when {
            normalized.length == 4 -> normalized[(x and 1) + (y and 1) * 2]
            else -> 'G'
        }
    }

    companion object {
        private const val BLOCK_SIZE = 2880
        const val MAX_FILE_BYTES = 128 * 1024 * 1024
        private const val MAX_RASTER_BYTES = 128L * 1024L * 1024L
        private const val MAX_RENDER_PIXELS = 4L * 1024L * 1024L

        fun parse(bytes: ByteArray): FitsDocument {
            require(bytes.size <= MAX_FILE_BYTES) { "FITS file exceeds the ${MAX_FILE_BYTES / (1024 * 1024)} MiB viewer limit" }
            val header = linkedMapOf<String, String>()
            var offset = 0
            var foundEnd = false
            while (offset + 80 <= bytes.size) {
                val card = bytes.decodeToString(offset, offset + 80)
                val key = card.substring(0, 8).trim()
                if (key == "END") { offset += 80; foundEnd = true; break }
                if (key.isNotEmpty() && card.getOrNull(8) == '=') {
                    val raw = card.substring(10).substringBefore("/").trim()
                    header[key] = raw.trim().trim('\'', '"').trim()
                }
                offset += 80
            }
            require(foundEnd) { "FITS header has no END card" }
            val dataOffset = ((offset + BLOCK_SIZE - 1) / BLOCK_SIZE) * BLOCK_SIZE
            val axes = header["NAXIS"]?.toIntOrNull() ?: error("Missing or invalid NAXIS")
            require(axes >= 2) { "FITS file does not contain a 2D image" }
            val width = header["NAXIS1"]?.toIntOrNull() ?: error("Missing or invalid NAXIS1")
            val height = header["NAXIS2"]?.toIntOrNull() ?: error("Missing or invalid NAXIS2")
            val bitpix = header["BITPIX"]?.toIntOrNull() ?: error("Missing or invalid BITPIX")
            require(width > 0 && height > 0) { "FITS image dimensions must be positive" }
            require(bitpix in setOf(8, 16, 32, 64, -32, -64)) { "Unsupported BITPIX $bitpix" }
            val planes = if (axes >= 3) header["NAXIS3"]?.toIntOrNull() ?: 1 else 1
            require(planes in 1..3) { "Only one-plane or RGB FITS images are supported" }
            val rasterBytes = width.toLong() * height * planes * (kotlin.math.abs(bitpix) / 8)
            require(rasterBytes <= MAX_RASTER_BYTES) { "FITS raster requires $rasterBytes bytes; limit is $MAX_RASTER_BYTES" }
            require(dataOffset.toLong() + rasterBytes <= bytes.size) { "FITS file is truncated: image data is incomplete" }
            val metadata = FitsMetadata(width, height, bitpix, planes,
                header["BSCALE"]?.toDoubleOrNull() ?: 1.0,
                header["BZERO"]?.toDoubleOrNull() ?: 0.0,
                header["BAYERPAT"]?.takeIf { it.isNotBlank() })
            return FitsDocument(bytes, dataOffset, metadata)
        }
    }
}
