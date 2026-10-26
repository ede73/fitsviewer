package fi.iki.ede.fits

import fi.iki.ede.fits.fits.FITSLoader
import fi.iki.ede.fits.fits.FITSRaster
import fi.iki.ede.fits.fits.datadefs.FITSPixelSize
import fi.iki.ede.fits.fits.datadefs.RGBColor
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.round

typealias RGB = Triple<Float, Float, Float>
typealias FITSColor = Pair<Float, RGBColor>

class RawFileTests {

    // helper class, FITS has coordinate system 0,0 top bottom
    // but fitsliberator I used visually to match pixels flips image by default, if unflipped
    // its coordinate system is flipped too, so its impossible to find visual match from image
    // unless this way
    class Coordinate(private val raster: FITSRaster) {
        private val height = raster.getBitmapSupport().fitsInfo.height

        fun getFlippedPixel(x: Int, y: Int): Pair<RGB, FITSColor> = getPixel(x, (height - 1) - y)

        fun getPixel(x: Int, y: Int): Pair<RGB, FITSColor> {
            val (value, color) = raster.rawFITSRasterBitmap.pixelReader.getPixel(x, y)
            val (r, g, b) = raster.debayeredBitmap.getDebayeredPixel(x, y)
            return Pair(RGB(r, g, b), FITSColor(value, color))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testRawM31() {
        val classLoader = javaClass.classLoader!!
        val inputStream = classLoader.getResourceAsStream("m31.fits")!!
        val loader = FITSLoader().apply {
            loadFITSStream(inputStream)
        }
        assertEquals(3072, loader.raster!!.fitsInfo.width)
        assertEquals(1.0, loader.raster!!.fitsInfo.pixelLimits.bScale)
        assertEquals(32768.0, loader.raster!!.fitsInfo.pixelLimits.bZero)
        assertEquals(
            FITSPixelSize.SIGNED_INT_16bit,
            loader.raster!!.fitsInfo.pixelLimits.pixelResolution
        )
        assertEquals(6144, loader.raster!!.getBitmapSupport().rowSizeOctets)
        assertEquals(
            loader.raster!!.fitsInfo.pixelLimits.bScale,
            loader.raster!!.getBitmapSupport().fitsInfo.pixelLimits.bScale
        )
        assertEquals(
            loader.raster!!.fitsInfo.pixelLimits.bZero,
            loader.raster!!.getBitmapSupport().fitsInfo.pixelLimits.bZero
        )

        loader.fitsHeader!!.headerValues.let {
            loader.fitsHeader!!.headerValues.let {
                assertEquals("T", it["SIMPLE"]!!.value)        // Standard FITS
                assertEquals("16", it["BITPIX"]!!.value)       // # bits/pixel
                assertEquals("2", it["NAXIS"]!!.value)         // Dimensions
                assertEquals("3072", it["NAXIS1"]!!.value)     // Picture width
                assertEquals("2080", it["NAXIS2"]!!.value)     // Picture height
                assertEquals("T", it["EXTEND"]!!.value)        // Extensions are permitted
                assertEquals(
                    "32768",
                    it["BZERO"]!!.value
                )     // offset data range to that of unsigned short
                assertEquals("1", it["BSCALE"]!!.value)        // default scaling factor
                assertEquals("'2021-11-25T03:05:59'", it["DATE-OBS"]!!.value) // Capture time (UTC)
                assertEquals("'stellina-8d7010'", it["INSTRUME"]!!.value) // STELLINA by Vaonis
                assertEquals("'BGGR    '", it["BAYERPAT"]!!.value) // Bayer pattern
                assertEquals("10000", it["EXPOSURE"]!!.value)  // [ms] Total Exposure Time
                assertEquals("200", it["GAIN"]!!.value)        // Sensor gain
                assertEquals("200", it["OFFSET"]!!.value)      // Camera brightness parameter
                assertEquals("19.1", it["TEMP"]!!.value)       // [C] Sensor temperature
                assertEquals("2.4", it["PIXSZ"]!!.value)       // [um] Pixel size
                assertEquals("400", it["FOCAL"]!!.value)       // [mm] Focal length
                assertEquals("77", it["WB_B"]!!.value)         // White balance (blue)
                assertEquals("64", it["WB_R"]!!.value)         // White balance (red)
                assertEquals("STELLINA by Vaonis", it["COMMENT"]!!.value)
            }
        }
        val c = Coordinate(loader.raster!!)
        //assertEquals(Triple(1767.0, 1432.0, 1933.0), c.get(0, 0))
        //assertEquals(Triple(0.0, 0.0, 0.0), c.get(loader.raster!!.width - 1, 0))

        // center of M31 (5007.6)
        assertEquals(
            Pair(RGB(6369.5f, 5216.0f, 5411.0f), FITSColor(5216.0f, RGBColor.G)),
            c.getFlippedPixel(1525, 1023)
        )
        // 1741.9
        assertEquals(
            Pair(RGB(1729.0f, 1438.0f, 2431.0f), Pair(2431.0f, RGBColor.B)),
            c.getFlippedPixel(0, loader.raster!!.fitsInfo.height - 1)
        )
        // 1431.9
        assertEquals(
            Pair(RGB(1509.5f, 1611.0f, 1933.0f), FITSColor(1933.0f, RGBColor.B)),
            c.getFlippedPixel(0, 1)
        )

        // bright star (1311.9!)
        assertEquals(
            Pair(RGB(1891.5f, 1720.0f, 3182.0f), FITSColor(1720.0f, RGBColor.G)),
            c.getFlippedPixel(25, 2075)
        )

        assertEquals(
            Pair(RGB(2154.0f, 1716.0f, 1624.0f), FITSColor(1716.0f, RGBColor.G)),
            c.getFlippedPixel(
                loader.raster!!.fitsInfo.width - 1,
                loader.raster!!.fitsInfo.height - 1
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun test4by43d() {
        val classLoader = javaClass.classLoader!!
        val inputStream = classLoader.getResourceAsStream("4x4_3_dimensional.fits")!!
        val loader = FITSLoader().apply {
            loadFITSStream(inputStream)
        }
        assertEquals(4, loader.raster!!.fitsInfo.width)
        assertEquals(4, loader.raster!!.fitsInfo.height)
        assertEquals(1.0, loader.raster!!.fitsInfo.pixelLimits.bScale)
        assertEquals(0.0, loader.raster!!.fitsInfo.pixelLimits.bZero)
        loader.fitsHeader!!.headerValues.let {
            assertEquals("T", it["SIMPLE"]!!.value)        // Standard FITS
            assertEquals("8", it["BITPIX"]!!.value)       // # bits/pixel
            assertEquals("3", it["NAXIS"]!!.value)         // Dimensions
            assertEquals("4", it["NAXIS1"]!!.value)     // Picture width
            assertEquals("4", it["NAXIS2"]!!.value)     // Picture height
            assertEquals("3", it["NAXIS3"]!!.value)     // Dimensions
            assertEquals("2.55000E+02", it["DATAMAX"]!!.value)
            assertEquals("0.00000E+00", it["DATAMIN"]!!.value)
        }
        val c = Coordinate(loader.raster!!)
        assertEquals(Pair(RGB(1f, 2f, 3f), FITSColor(1f, RGBColor.BW)), c.getPixel(0, 0))
        assertEquals(Pair(RGB(4f, 5f, 6f), FITSColor(4f, RGBColor.BW)), c.getPixel(1, 0))
        assertEquals(Pair(RGB(7f, 8f, 9f), FITSColor(7f, RGBColor.BW)), c.getPixel(2, 0))

        assertEquals(Pair(RGB(10f, 20f, 30f), FITSColor(10f, RGBColor.BW)), c.getPixel(0, 1))
        assertEquals(
            Pair(RGB(40f, 50f, 60f), FITSColor(40f, RGBColor.BW)),
            c.getPixel(1, 1)
        )
        assertEquals(
            Pair(RGB(70f, 80f, 90f), FITSColor(70f, RGBColor.BW)),
            c.getPixel(2, 1)
        )
        assertEquals(
            Pair(RGB(4f, 4f, 4f), FITSColor(4f, RGBColor.BW)),
            c.getPixel(3, 3)
        )
    }
}

fun Pair<RGB, FITSColor>.rr(precision: Float): Pair<RGB, FITSColor> {
    return Pair(
        RGB(
            (round(first.first / precision) * precision),
            (round(first.second / precision) * precision),
            (round(first.third / precision) * precision)
        ), FITSColor((second.first / precision) * precision, second.second)
    )
}