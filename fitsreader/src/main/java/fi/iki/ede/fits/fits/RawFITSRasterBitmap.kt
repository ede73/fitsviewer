package fi.iki.ede.fits.fits

import fi.iki.ede.fits.fits.datadefs.FITSInfo
import fi.iki.ede.fits.fits.datadefs.FITSPixelSize
import fi.iki.ede.fits.fits.parsing.FITSHeaderDataUnit
import fi.iki.ede.fits.fits.parsing.RawFITSPixel
import fi.iki.ede.fits.fits.stream.TrackingInputStream

private const val TAG = "RasterBitmap"

// anything labelled here as 16bits can actually be 8-64bits...(check fits)
// its just a warning that the COLOR isn't 8bit color!
class RawFITSRasterBitmap(val fitsInfo: FITSInfo) {
    // Return size of one ROW - due to bayer pattern it doesn't mean all the pixel are there (bayering)
    val pixelBitSpecification = fitsInfo.pixelLimits.pixelResolution
    private val rawRasterSize = computeRawSize(fitsInfo.width, fitsInfo.height, pixelBitSpecification.getBitDepth(), fitsInfo.naxis3 ?: 1)
    val rowSizeOctets = fitsInfo.width * (pixelBitSpecification.getBitDepth() / 8)
    val rawFITSBitmap = ByteArray(rawRasterSize)
    val pixelReader = RawFITSPixel(this)

    init {
        require(fitsInfo.width < 5000) { "Raster width set as $fitsInfo.width, let's be reasonable with size!" }
        require(fitsInfo.height < 5000) { "Raster height set as $fitsInfo.height, let's be reasonable with size!" }
        require(
            pixelBitSpecification == FITSPixelSize.SIGNED_INT_16bit ||
                    pixelBitSpecification == FITSPixelSize.UNSIGNED_8bit ||
                    pixelBitSpecification == FITSPixelSize.IEEE_FLOAT_32bit ||
                    pixelBitSpecification == FITSPixelSize.TWOS_COMPLEMENT_32bit
        ) { "Alas only supporting 16/-32 bits per pixel modes currently, input $pixelBitSpecification" }
    }

    private fun computeRawSize(width: Int, height: Int, bitDepth: Int, planes: Int): Int {
        val bytes = width.toLong() * height * (bitDepth / 8) * planes
        require(bytes in 1..MAX_RAW_RASTER_BYTES) {
            "FITS raster requires $bytes bytes; this viewer currently supports up to $MAX_RAW_RASTER_BYTES raw bytes per image"
        }
        return bytes.toInt()
    }

    companion object {
        private const val MAX_RAW_RASTER_BYTES = 128L * 1024L * 1024L
    }

    override fun toString(): String {
        val data = StringBuilder()
        for (row in 0 until fitsInfo.height) {
            val rowStart = row * fitsInfo.width
            val rowEnd = rowStart + fitsInfo.width
            val rowValues = rawFITSBitmap.slice(rowStart until rowEnd)

            val hexRow = rowValues.joinToString(" ") { byteValue ->
                byteValue.toUByte().toString(16).padStart(2, '0')
            }
            data.append(hexRow)
            data.append("\n")
        }
        return data.toString()
    }

    fun read(rasterStream: TrackingInputStream) {
        assert(rasterStream.getPosition() % FITSHeaderDataUnit.DEFAULT_HDU_SIZE == 0L) { "Pixel data not aligned at default HDU size" }
        val read = rasterStream.read(rawFITSBitmap)
        assert(
            read == rawFITSBitmap.size
        ) { "Could not read as many bytes from raster as required, got $read, required ${rawFITSBitmap.size}" }

        //pixelLimit.minValueRead = bitmap.min() //TODO: where to fill, also depends on interpretation/bayering!?
        //pixelLimit.minValueRead = bitmap.max() //TODO: where to fill, also depends on interpretation/bayering!?
        rasterStream.skipToDefaultBlock()
    }
}
