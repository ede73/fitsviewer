package fi.iki.ede.fits

import fi.iki.ede.fits.fits.datadefs.BayerPattern
import fi.iki.ede.fits.fits.datadefs.RGBColor
import org.junit.Assert.assertEquals
import org.junit.Test

class BayerPatternTest {
//    @Test
//    fun testBGGRPattern() {
//        val bayerPattern = BayerPattern("BGGR")
//
//        // Row0 B[0001] G[0012] B(0023) G(0034)
//        // Row1 G(0045) R[0056] G(0067) R(0078)
//        // Row2 B(0089) G(009a) B(00ab) G(00bc)
//        // Row0=BG BG Row1=GR GR
//        // Test even line
//        assertEquals(Pair(1, 1), bayerPattern.getIndex(RGBColor.R, true))
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.G, true))
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.B, true))
//
//        // Row0 B(0001) G(0012) B(0023) G(0034)
//        // Row1 G[0045] R[0056] G(0067) R(0078)
//        // Row2 B[0089] G(009a) B(00ab) G(00bc)
//        // Row0=BG BG Row1=GR GR
//        // Test odd line
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.R, false))
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.G, false))
//        assertEquals(Pair(0, 1), bayerPattern.getIndex(RGBColor.B, false))
//    }

    @Test
    fun testBGGRPatternIndex() {
        val bayerPattern = BayerPattern("BGGR")
        // Test even line
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(0, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(1, 0))
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(2, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(3, 0))

        // Test odd line
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(0, 1))
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(1, 1))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(2, 1))
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(3, 1))
    }

//    @Test
//    fun testRGBGPattern() {
//        val bayerPattern = BayerPattern("RGBG")
//
//        // Row0 R[0001] G[0012] R(0023) G(0034)
//        // Row1 B[0045] G(0056) B(0067) G(0078)
//        // Row2 R(0089) G(009a) R(00ab) G(00bc)
//        // Row0=RG RG Row1=BG BG
//        // Test even line
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.R, true))
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.G, true))
//        assertEquals(Pair(0, 1), bayerPattern.getIndex(RGBColor.B, true))
//
//        // Row0 R(0001) G(0012) R(0023) G(0034)
//        // Row1 B[0045] G[0056] B(0067) G(0078)
//        // Row2 R[0089] G(009a) R(00ab) G(00bc)
//        // Row0=RG RG Row1=BG BG
//        // Test odd line
//        assertEquals(Pair(0, 1), bayerPattern.getIndex(RGBColor.R, false))
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.G, false))
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.B, false))
//    }

    @Test
    fun testRGBGPatternIndex() {
        val bayerPattern = BayerPattern("RGBG")
        // Test even line
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(0, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(1, 0))
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(2, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(3, 0))

        // Test odd line
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(0, 1))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(1, 1))
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(2, 1))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(3, 1))
    }

//    @Test
//    fun testGRBGPattern() {
//        val bayerPattern = BayerPattern("GRBG")
//
//        // Row0 G[0001] R[0012] G(0023) R(0034)
//        // Row1 B[0045] G(0056) B(0067) G(0078)
//        // Row2 G(0089) R(009a) G(00ab) R(00bc)
//        // Row0=GR GR Row1=BG BG
//        // Test even line
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.R, true))
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.G, true))
//        assertEquals(Pair(0, 1), bayerPattern.getIndex(RGBColor.B, true))
//
//        // Row0 G(0001) R(0012) G(0023) R(0034)
//        // Row1 B[0045] G[0056] B(0067) G(0078)
//        // Row2 G(0089) R[009a] G(00ab) R(00bc)
//        // Row0=GR GR Row1=BG BG
//        // Test odd line
//        assertEquals(Pair(1, 1), bayerPattern.getIndex(RGBColor.R, false))
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.G, false))
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.B, false))
//    }

    @Test
    fun testGRBGPatternIndex() {
        val bayerPattern = BayerPattern("GRBG")
        // Test even line
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(0, 0))
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(1, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(2, 0))
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(3, 0))

        // Test odd line
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(0, 1))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(1, 1))
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(2, 1))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(3, 1))
    }

//    @Test
//    fun testRGGBPattern() {
//        val bayerPattern = BayerPattern("RGGB")
//
//        // Row0 R[0] G[1] R{2} G{3}
//        // Row1 G(4) B[5] G/6/ B{7}
//        // Row2 R(8) G(9) R/10/ G/11/
//        // Row0=RGRG Row1=GBGB
//
//        // Test even line
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.R, true)) // 0
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.G, true)) // 1
//        assertEquals(Pair(1, 1), bayerPattern.getIndex(RGBColor.B, true)) // 5
//
//        // Row0 R[0] G[1] R{2} G{3}
//        // Row1 G(4) B[5] G/6/ B{7}
//        // Row2 R(8) G(9) R/10/ G/11/
//        // Row0=RGRG Row1=GBGB
//        // Test odd line
//        assertEquals(Pair(0, 1), bayerPattern.getIndex(RGBColor.R, false)) // 8
//        assertEquals(Pair(0, 0), bayerPattern.getIndex(RGBColor.G, false)) // 4
//        assertEquals(Pair(1, 0), bayerPattern.getIndex(RGBColor.B, false)) // 5
//    }

    @Test
    fun testRGGBPatternIndex() {
        val bayerPattern = BayerPattern("RGGB")
        // Test even line
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(0, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(1, 0))
        assertEquals(RGBColor.R, bayerPattern.getColorAtIndex(2, 0))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(3, 0))

        // Test odd line
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(0, 1))
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(1, 1))
        assertEquals(RGBColor.G, bayerPattern.getColorAtIndex(2, 1))
        assertEquals(RGBColor.B, bayerPattern.getColorAtIndex(3, 1))
    }
}