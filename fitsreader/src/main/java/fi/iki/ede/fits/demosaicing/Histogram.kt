package fi.iki.ede.fits.demosaicing

import android.graphics.Color
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Histogram(
    width: Int, height: Int, minRGB: Float, maxRGB: Float, debayeredBitmap: Buffer,
    precomputedBins: IntArray? = null
) {
    val histogram = IntArray(256)
    val maxCount: Int

    constructor(bins: IntArray) : this(0, 0, 0f, 0f, FloatBuffer.allocate(0), bins)

    init {
        if (precomputedBins != null) {
            require(precomputedBins.size == histogram.size)
            precomputedBins.copyInto(histogram)
            maxCount = histogram.maxOrNull() ?: 1
        } else {
          require(minRGB <= maxRGB) { "minRGB $minRGB >= maxRGB $maxRGB" }
          val variance = (maxRGB - minRGB).let { if (it <= 0) 1f else it }
          require(variance > 0) { "variance $variance" }
          for (x in 0 until width) {
            for (y in 0 until height) {

                val luminance = when (debayeredBitmap) {
                    is FloatBuffer -> {
                        // TODO: only if 16bit
                        debayeredBitmap.position(y * height * 3 + x * 3)
                        val (r, g, b) = Triple(
                            255f * (debayeredBitmap.get() - minRGB) / variance,
                            255f * (debayeredBitmap.get() - minRGB) / variance,
                            255f * (debayeredBitmap.get() - minRGB) / variance
                        )
                        //max(0, min(65535, ((r + g + b) / 3.0).toInt())) / 256
                        (0.2126 * r + 0.7152 * g + 0.0722 * b).toInt()
                    }

                    is IntBuffer -> {
                        // TODO: only if 16bit
                        debayeredBitmap.position(y * height + x)
                        val colorRgb = Color.valueOf(debayeredBitmap.get())
                        //max(0, min(65535, ((r + g + b) / 3.0).toInt())) / 256
                        //colorRgb.luminance()
                        (0.2126 * colorRgb.red() + 0.7152 * colorRgb.green() + 0.0722 * colorRgb.blue()).toInt()
                    }

                    else -> throw IllegalArgumentException("Not supported")
                }
                histogram[luminance.coerceIn(0, 255)]++
            }
          }
          maxCount = histogram.maxOrNull() ?: 1
        }
    }
}
