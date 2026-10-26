/*
 * Copyright (c) Erkka Marjakangas 2023 . All rights reserved.
 *
 * See LICENCE for further details.
 */

package fi.iki.ede.fits

import fi.iki.ede.fits.fits.FITSRaster
import fi.iki.ede.fits.fits.PixelLimits
import fi.iki.ede.fits.fits.datadefs.BayerPattern
import fi.iki.ede.fits.fits.datadefs.FITSInfo
import fi.iki.ede.fits.fits.datadefs.FITSPixelSize
import fi.iki.ede.fits.fits.datadefs.RGBColor
import fi.iki.ede.fits.fits.hexToByteArray
import fi.iki.ede.fits.fits.stream.FITSInputStream
import fi.iki.ede.fits.fits.stream.TrackingInputStream
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class RawFITSRasterBitmapTests {

    @Test
    fun stateTest() {
        val r = FITSRaster(
            TrackingInputStream(
                ByteArrayInputStream(
                    ("""0001 0012 0023 0034
                        0045 0056 0067 0078
                        0089 009A 00Ab 00bc""".replace(
                        "\n",
                        ""
                    ).replace(" ", "") + "00".repeat(2880 - 8 * 3)).hexToByteArray()
                )
            ),
            FITSInfo(
                4, 3,
                BayerPattern("RGGB"),
                PixelLimits(
                    FITSPixelSize.SIGNED_INT_16bit,
                    1.0,
                    0.0
                ),
                null
            ),
        )

        println(r.getBitmapSupport().toString())

        assertEquals(4 * 2, r.getBitmapSupport().rowSizeOctets)
        assertEquals(0.0, r.fitsInfo.pixelLimits.bZero)
        assertEquals(1.0, r.fitsInfo.pixelLimits.bScale)
        val readPixel = r.rawFITSRasterBitmap.pixelReader

        // bZero = 1.0, bScale = 1.0, SIGNED_INT_16bit
        // Row0 R[0001] G[0012] R(0023) G(0034)
        // Row1 G(0045) B[0056] G(0067) B(0078)
        // Row2 R(0089) G(009a) R(00ab) G(00bc)
        // Row0=RGRG Row1=GBGB
        assertEquals(Pair((0x01).toFloat(), RGBColor.R), readPixel.getPixel(0, 0))

        // bZero = 32768.0, bScale = 1.0, SIGNED_INT_16bit
        // Row0 R(0001) G(0012) R[0023] G[0034]
        // Row1 G(0045) B(0056) G(0067) B[0078]
        // Row2 R(0089) G(009a) R(00ab) G(00bc)
        // Row0=RGRG Row1=GBGB
        assertEquals(Pair((0x12).toFloat(), RGBColor.G), readPixel.getPixel(1, 0))

        // bZero = 32768.0, bScale = 1.0, SIGNED_INT_16bit
        // Row0 R(0001) G(0012) R(0023) G(0034)
        // Row1 G[0045] B[0056] G(0067) B(0078)
        // Row2 R[0089] G(009a) R(00ab) G(00bc)
        // Row1=GBGB Row2=RGRG
        assertEquals(Pair((0x45).toFloat(), RGBColor.G), readPixel.getPixel(0, 1))

        // bZero = 32768.0, bScale = 1.0, SIGNED_INT_16bit
        // Row0 R(0001) G(0012) R(0023) G(0034)
        // Row1 G(0045) B(0056) G[0067] B[0078]
        // Row2 R(0089) G(009a) R[00ab] G(00bc)
        // Row1=GBGB Row2=RGRG
        assertEquals(Pair((0x56).toFloat(), RGBColor.B), readPixel.getPixel(1, 1))
    }

    @Test
    fun loadRaster() {
        val fitsStream = this.javaClass.classLoader?.getResourceAsStream("IMG_0010.fits")
        val fits = FITSInputStream(fitsStream!!)
        print(fits.fitsHeader.toString())
        println(fits.fitsRaster.toString())

        val bZero = fits.fitsRaster.fitsInfo.pixelLimits.bZero
        val bScale = fits.fitsRaster.fitsInfo.pixelLimits.bScale

        assertEquals(
            FITSPixelSize.SIGNED_INT_16bit,
            fits.fitsRaster.fitsInfo.pixelLimits.pixelResolution
        )
        assertEquals(1.0, bScale)
        assertEquals(32768.0, bZero)

        // bZero = 32768.0, bScale = 1.0, SIGNED_INT_16bit
        // Row0 R(0001) G(0012) R(0023) G(0034)
        // Row1 G(0045) B(0056) G(0067) B(0078)
        // Row2 R(0089) G(009a) R(00ab) G(00bc)

        // (RR)(GG).. 3072*2 bytes
        // (GG)(BB).. 3072*2 bytes
        assertEquals(3072 * 2, fits.fitsRaster.getBitmapSupport().rowSizeOctets)

        assertEquals("RGGB", fits.fitsRaster.getBitmapSupport().fitsInfo.bayerPattern.pattern)

        val readPixel = fits.fitsRaster.rawFITSRasterBitmap.pixelReader

        // bZero = 32768.0, bScale = 1.0, SIGNED_INT_16bit
        // 0x0b40 0x0000(    0) Row0 R(8414) G(8328) R[840f] G[8324] 83f5 8324 8419 833c
        // 0x2340 0x1800( 6144) Row1 G(830c) B(84b6) G(8340) B[84b0] 832c 84b6 831c 84bc
        // 0x3B40 0x3000(12288) Row2 R(8340) G(8328) R(840f) G(8324) 83f5 8324 8419 833c
        // Row0=RGRG Row1=GBGB
        assertEquals(Pair(1044f, RGBColor.R), readPixel.getPixel(0, 0))
        assertEquals(Pair(780f, RGBColor.G), readPixel.getPixel(0, 1))
        assertEquals(Pair(808f, RGBColor.G), readPixel.getPixel(1, 0))
        assertEquals(Pair(1206f, RGBColor.B), readPixel.getPixel(1, 1))
    }
}