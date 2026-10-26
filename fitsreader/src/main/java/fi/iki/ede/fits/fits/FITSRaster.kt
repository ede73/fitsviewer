package fi.iki.ede.fits.fits

import androidx.annotation.VisibleForTesting
import fi.iki.ede.fits.demosaicing.DebayeredBitmap
import fi.iki.ede.fits.fits.datadefs.FITSInfo
import fi.iki.ede.fits.fits.stream.TrackingInputStream

typealias BayerPixelIndex = Pair<Int, Int>

// See https://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html
// physical_value = BZERO + BSCALE * array_value
// only really supports 16bits per pixel
// also expects bayer pattern specified!
@Deprecated("TODO: REMOVE< TEMP")
class FITSRaster(
    rasterStream: TrackingInputStream,
    val fitsInfo: FITSInfo,
    skipRasterData: Boolean = false
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val rawFITSRasterBitmap: RawFITSRasterBitmap = RawFITSRasterBitmap(fitsInfo)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val debayeredBitmap: DebayeredBitmap

    init {
        if (!skipRasterData) {
            rawFITSRasterBitmap.read(rasterStream)
        }
        debayeredBitmap = DebayeredBitmap(rawFITSRasterBitmap)
    }

    @Deprecated("fdsshdfshdf")
    fun getBitmapSupport(): DebayeredBitmap = debayeredBitmap

    override fun toString() =
        "Raster width:${fitsInfo.width}, height:${fitsInfo.height}, bits per pixel:${fitsInfo.pixelLimits.pixelResolution.getBitDepth()}, bayer:${fitsInfo.bayerPattern.pattern}"
}