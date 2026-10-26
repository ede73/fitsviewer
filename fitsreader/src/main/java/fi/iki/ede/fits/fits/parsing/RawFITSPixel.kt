package fi.iki.ede.fits.fits.parsing

import fi.iki.ede.fits.fits.RawFITSRasterBitmap
import fi.iki.ede.fits.fits.datadefs.FITSPixelSize
import fi.iki.ede.fits.fits.datadefs.RGBColor

class RawFITSPixel(private val rawFITSRasterBitmap: RawFITSRasterBitmap) {
    val octetsPerPixel = rawFITSRasterBitmap.pixelBitSpecification.getBitDepth() / 8

    // http://www.astro.sunysb.edu/fwalter/AST443/fits.html#:~:text=A%20FITS%20file%20(on%20tape,this%20to%20increase%20storage%20efficiency.
    fun getPixel(x: Int, y: Int): Pair<Float, RGBColor> {
        val index = y * rawFITSRasterBitmap.rowSizeOctets + x * octetsPerPixel
        assert(index < rawFITSRasterBitmap.rawFITSBitmap.size) { "Index for ($x,$y) points to $index while raster only has ${rawFITSRasterBitmap.rawFITSBitmap.size} bytes when width=${rawFITSRasterBitmap.fitsInfo.width} height=${rawFITSRasterBitmap.fitsInfo.height}, octetsPerPixel=$octetsPerPixel, rawFITSRasterBitmap.rowSizeOctets=${rawFITSRasterBitmap.rowSizeOctets}" }
        assert(index >= 0) { "Reading negative index $index" }
        return Pair(
            readPixel(index).toFloat(),
            rawFITSRasterBitmap.fitsInfo.bayerPattern.getColorAtIndex(x, y)
        )
    }

    private fun readPixel(index: Int): Double {
        when (rawFITSRasterBitmap.pixelBitSpecification) {
            FITSPixelSize.SIGNED_INT_16bit -> {
                // Variances? Stellina uses bayer pattern, so this is actually
                // 8 bits per color, and only two colors (one is extrapolated)
                assert(index + 1 < rawFITSRasterBitmap.rawFITSBitmap.size) { "Index points to ${index + 1} while raster only has ${rawFITSRasterBitmap.rawFITSBitmap.size} bytes}" }
                val bytes = rawFITSRasterBitmap.rawFITSBitmap
                val value = (((bytes[index].toInt() and 0xff) shl 8) or (bytes[index + 1].toInt() and 0xff)).toShort().toDouble()
                // According to https://heasarc.gsfc.nasa.gov/docs/software/fitsio/c/c_user/node26.html?form=MG0AV3
                return value * rawFITSRasterBitmap.fitsInfo.pixelLimits.bScale + rawFITSRasterBitmap.fitsInfo.pixelLimits.bZero
            }

            FITSPixelSize.IEEE_FLOAT_32bit -> {
                //TODO: always big endian
                // https://fits.gsfc.nasa.gov/fp89.txt
                // -32 = IEEE single precision
                assert(index + 3 < rawFITSRasterBitmap.rawFITSBitmap.size) { "Index points to ${index + 3} while raster only has ${rawFITSRasterBitmap.rawFITSBitmap.size} bytes}" }
                val bytes = rawFITSRasterBitmap.rawFITSBitmap
                val bits = ((bytes[index].toInt() and 0xff) shl 24) or
                    ((bytes[index + 1].toInt() and 0xff) shl 16) or
                    ((bytes[index + 2].toInt() and 0xff) shl 8) or
                    (bytes[index + 3].toInt() and 0xff)
                val float = Float.fromBits(bits)
                return float.toDouble()
            }

            FITSPixelSize.TWOS_COMPLEMENT_32bit -> {
                // Read 4 bytes and convert to a 32-bit signed integer using two's complement
                assert(index + 3 < rawFITSRasterBitmap.rawFITSBitmap.size) { "Index points to ${index + 3} while raster only has ${rawFITSRasterBitmap.rawFITSBitmap.size} bytes}" }
                val bytes = rawFITSRasterBitmap.rawFITSBitmap
                val value = ((bytes[index].toInt() and 0xff) shl 24) or
                    ((bytes[index + 1].toInt() and 0xff) shl 16) or
                    ((bytes[index + 2].toInt() and 0xff) shl 8) or
                    (bytes[index + 3].toInt() and 0xff)
                return value * rawFITSRasterBitmap.fitsInfo.pixelLimits.bScale + rawFITSRasterBitmap.fitsInfo.pixelLimits.bZero
            }

            else -> {
                assert(index < rawFITSRasterBitmap.rawFITSBitmap.size) { "Index points to ${index} while raster only has ${rawFITSRasterBitmap.rawFITSBitmap.size} bytes}" }
                val value = rawFITSRasterBitmap.rawFITSBitmap[index].toUByte().toDouble()
                // According to https://heasarc.gsfc.nasa.gov/docs/software/fitsio/c/c_user/node26.html?form=MG0AV3
                return value * rawFITSRasterBitmap.fitsInfo.pixelLimits.bScale + rawFITSRasterBitmap.fitsInfo.pixelLimits.bZero
            }
        }
    }
}
