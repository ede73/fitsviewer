package fi.iki.ede.fits.fits.datadefs

import fi.iki.ede.fits.fits.PixelLimits

data class FITSInfo(
    val width: Int,
    val height: Int,
    val bayerPattern: BayerPattern,
    val pixelLimits: PixelLimits,
    val naxis3: Int?
)