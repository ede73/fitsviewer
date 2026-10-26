package fi.iki.ede.fits.fits.datadefs

import kotlin.math.absoluteValue

enum class FITSPixelSize(private val bitsPerPixel: Int) {
    UNSIGNED_8bit(8),
    SIGNED_INT_16bit(16),
    TWOS_COMPLEMENT_32bit(32),
    IEEE_FLOAT_32bit(-32),
    IEE_FLOAT_64bit(-64);

    fun getBitDepth(): Int = bitsPerPixel.absoluteValue

    companion object {
        infix fun from(bitsPerPixel: Int): FITSPixelSize? =
            FITSPixelSize.entries.firstOrNull { it.bitsPerPixel == bitsPerPixel }
    }
}