package fi.iki.ede.fits.fits.stream

import fi.iki.ede.fits.fits.FITSRaster
import fi.iki.ede.fits.fits.PixelLimits
import fi.iki.ede.fits.fits.datadefs.BayerPattern
import fi.iki.ede.fits.fits.datadefs.FITSInfo
import fi.iki.ede.fits.fits.datadefs.FITSPixelSize
import fi.iki.ede.fits.fits.parsing.FITSHeaderDataUnit
import java.io.InputStream

private val TAG = "FITSInputStream"

/*
After the primary HDU you might have extra HDUs

HDU 0 (Primary HDU)
  - Header
  - Image Data

HDU 1
  - Header
  - Image Data

HDU 2
  - Header
  - Image Data
 */

class FITSInputStream(inputStream: InputStream, skipRasterData: Boolean = false) {
    private val trackingInputStream = TrackingInputStream(inputStream)
    val fitsHeader: FITSHeaderDataUnit = FITSHeaderDataUnit(trackingInputStream)
    val fitsRaster: FITSRaster

    init {
        val naxis = fitsHeader.headerValues["NAXIS"]?.getInt()!!
        val width = fitsHeader.headerValues["NAXIS1"]?.getInt()!!
        val height = fitsHeader.headerValues["NAXIS2"]?.getInt()!!
        val bitsPerPixel = fitsHeader.headerValues["BITPIX"]?.getInt()!!
        val bZero = fitsHeader.headerValues["BZERO"]?.getDouble()!!
        val bScale = fitsHeader.headerValues["BSCALE"]?.getDouble()!!
        val bayerPat = fitsHeader.headerValues["BAYERPAT"]?.getString()
        // crude short cut to dimensionally arranged FITS
        val nAxis3 = fitsHeader.headerValues["NAXIS3"]?.getInt()

        trackingInputStream.skipToDefaultBlock()
        fitsRaster = FITSRaster(
            trackingInputStream,
            FITSInfo(
                width, height,
                if (bayerPat == null) BayerPattern() else BayerPattern(bayerPat),
                PixelLimits(
                    FITSPixelSize.from(bitsPerPixel)!!,
                    bScale,
                    bZero,
                ),
                nAxis3
            ), skipRasterData
        )

        if (!skipRasterData) {
            assert(trackingInputStream.getPosition() % FITSHeaderDataUnit.DEFAULT_HDU_SIZE == 0L) { "HDU not aligned at default size" }
            var unreadBytes = 0
            // how much extra we have unread!?
            while (trackingInputStream.read() != -1) {
                unreadBytes++
            }
            //Log.e(TAG, "Unread bytes $unreadBytes")
            //assert(unreadBytes == 0) { "Oops! There's some extra data (secondary PDU? Total of $unreadBytes) " }
        }
    }
}