package fi.iki.ede.fits.fits

import android.content.Context
import fi.iki.ede.fits.fits.parsing.FITSHeaderDataUnit
import fi.iki.ede.fits.fits.stream.FITSInputStream
import java.io.InputStream

// https://fits.gsfc.nasa.gov/standard30/fits_standard30aa.pdf
// https://siril.readthedocs.io/en/latest/file-formats/FITS.html
class FITSLoader {
    var raster: FITSRaster? = null
    var fitsHeader: FITSHeaderDataUnit? = null

    fun loadFITS(context: Context, fitsResource: Int) =
        loadFITSStream(context.resources.openRawResource(fitsResource))

    fun loadFITSStream(fitsStream: InputStream) {
        val fits = FITSInputStream(fitsStream)
        fitsHeader = fits.fitsHeader
        val nAxis = fitsHeader?.headerValues?.get("NAXIS")?.getInt()!!
//        val width = fitsHeader?.headerValues["NAXIS1"]?.getInt()!!
//        val height = fitsHeader?.headerValues["NAXIS2"]?.getInt()!!
//        val bitsPerPixel = fitsHeader.headerValues["BITPIX"]?.getInt()!!
//        val bZero = fitsHeader?.headerValues["BZERO"]?.getDouble()!!
//        val bScale = fitsHeader?.headerValues["BSCALE"]?.getDouble()!!
//        val bayer = fitsHeader?.headerValues["BAYERPAT"]?.getString()
        raster = fits.fitsRaster
    }
}