package fi.iki.ede.fits.fits

import fi.iki.ede.fits.fits.datadefs.FITSPixelSize

class PixelLimits(
    val pixelResolution: FITSPixelSize,
    val bScale: Double,
    val bZero: Double,
) {
    var minValueRead: Double = Double.MAX_VALUE
    var maxValueRead: Double = Double.MIN_VALUE

    private val bitDepthMaxValue = (1L shl pixelResolution.getBitDepth()) - 1

    private fun scale(value: Double): Double = (value + bZero) * bScale

    fun getMaxValue(): Double = when (pixelResolution) {
        FITSPixelSize.UNSIGNED_8bit -> scale(255.0)
        FITSPixelSize.SIGNED_INT_16bit -> scale(32767.0)
        FITSPixelSize.IEEE_FLOAT_32bit -> scale(Int.MAX_VALUE.toDouble()) //TODO:
        FITSPixelSize.IEE_FLOAT_64bit -> scale(Int.MAX_VALUE.toDouble()) //TODO:
        FITSPixelSize.TWOS_COMPLEMENT_32bit -> scale(Int.MAX_VALUE.toDouble())//TODO:
    }

    fun getMinValue(): Double = when (pixelResolution) {
        FITSPixelSize.UNSIGNED_8bit -> scale(0.0)
        FITSPixelSize.SIGNED_INT_16bit -> scale(-32768.0)
        FITSPixelSize.IEEE_FLOAT_32bit -> scale(-32768.0) // TODO:
        FITSPixelSize.IEE_FLOAT_64bit -> scale(-32768.0) // TODO:
        FITSPixelSize.TWOS_COMPLEMENT_32bit -> scale(-32768.0) // TODO:
    }
}