package fi.iki.ede.fits

import fi.iki.ede.fits.fits.PixelLimits
import fi.iki.ede.fits.fits.RawFITSRasterBitmap
import fi.iki.ede.fits.fits.datadefs.BayerPattern
import fi.iki.ede.fits.fits.datadefs.FITSInfo
import fi.iki.ede.fits.fits.datadefs.FITSPixelSize
import fi.iki.ede.fits.fits.datadefs.RGBColor
import fi.iki.ede.fits.fits.parsing.RawFITSPixel
import junit.framework.TestCase.assertEquals
import org.junit.Test

class RawFITSPixelTests {

    @Test
    fun indexTestRGGBSignetInt16() {
        // octersPerPixel = 2
        // rowSizeOctets = 4
        // pixel = 16 bits
        //      X Pixel 0     X Pixel 1
        // Row0 R[ 0] G[ 2]   R[ 4] G[ 6]
        // Row1 G[ 8] B[10]   G[12] B[14]
        //      X Pixel 0     X Pixel 1
        // Row2 R[16] G[18]   R[20] G[22]
        // Row3 G{24} B[26]   G{28} B[30]
        val bm = RawFITSRasterBitmap(
            FITSInfo(
                4, 4,
                BayerPattern("RGGB"),
                PixelLimits(FITSPixelSize.SIGNED_INT_16bit, 0.0, 1.0),
                null
            )
        )
        val p = RawFITSPixel(bm)
        assertEquals(2, p.octetsPerPixel)
        assertEquals(4, bm.fitsInfo.width)
        assertEquals(4, bm.fitsInfo.height)
        assertEquals(p.octetsPerPixel * bm.fitsInfo.width, bm.rowSizeOctets)
        assertEquals(8, bm.rowSizeOctets)
        assertEquals(4 * 4 * 2, bm.rawFITSBitmap.size)

        assertEquals(Pair(1.0f, RGBColor.B), p.getPixel(3, 3))
        assertEquals(Pair(1.0f, RGBColor.R), p.getPixel(0, 0))
        assertEquals(Pair(1.0f, RGBColor.G), p.getPixel(0, 1))
        assertEquals(Pair(1.0f, RGBColor.G), p.getPixel(1, 0))
        assertEquals(Pair(1.0f, RGBColor.B), p.getPixel(1, 1))
        assertEquals(Pair(1.0f, RGBColor.R), p.getPixel(0, 2))
        assertEquals(Pair(1.0f, RGBColor.G), p.getPixel(1, 2))

    }
}