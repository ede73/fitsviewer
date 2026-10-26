package fi.iki.ede.fits.demosaicing

import fi.iki.ede.fits.fits.PixelLimits
import fi.iki.ede.fits.fits.datadefs.BayerPattern
import java.nio.FloatBuffer

class SimpleAverage : Demosaic {
    override fun getRed(
        x: Int,
        y: Int,
        buffer: FloatBuffer,
        bayer: BayerPattern,
        pixelLimits: PixelLimits
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getGreen(
        x: Int,
        y: Int,
        buffer: FloatBuffer,
        bayer: BayerPattern,
        pixelLimits: PixelLimits
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getBlue(
        x: Int,
        y: Int,
        buffer: FloatBuffer,
        bayer: BayerPattern,
        pixelLimits: PixelLimits
    ): Int {
        TODO("Not yet implemented")
    }

}