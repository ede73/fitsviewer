package fi.iki.ede.fits.demosaicing

import fi.iki.ede.fits.fits.PixelLimits
import fi.iki.ede.fits.fits.datadefs.BayerPattern
import java.nio.FloatBuffer

interface Demosaic {
    fun getRed(
        x: Int,
        y: Int,
        buffer: FloatBuffer,
        bayer: BayerPattern,
        pixelLimits: PixelLimits
    ): Int

    fun getGreen(
        x: Int,
        y: Int,
        buffer: FloatBuffer,
        bayer: BayerPattern,
        pixelLimits: PixelLimits
    ): Int

    fun getBlue(
        x: Int,
        y: Int,
        buffer: FloatBuffer,
        bayer: BayerPattern,
        pixelLimits: PixelLimits
    ): Int
}