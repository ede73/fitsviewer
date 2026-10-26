package fi.iki.ede.fits.fits.datadefs

import kotlin.math.abs

class BayerPattern(val pattern: String = GRAYSCALE) {
    private val pixelPatternIndex: Array<Array<RGBColor>>

    init {
        check(COLOR_LOOKUP.containsKey(pattern)) { "Oh no! Bayer pattern $pattern not found" }
        pixelPatternIndex = COLOR_LOOKUP[pattern]!!
    }

    fun getColorAtIndex(x: Int, y: Int) = pixelPatternIndex[abs(x) % 2][abs(y) % 2]

    companion object {
        const val GRAYSCALE = ""

        private val COLOR_LOOKUP = HashMap<String, Array<Array<RGBColor>>>().apply {
            for (pattern in listOf("BGGR", "RGBG", "GRBG", "RGGB", GRAYSCALE)) {
                put(
                    pattern,
                    createBayerPatternArray(pattern)
                )
            }
        }

        private fun createBayerPatternArray(pattern: String): Array<Array<RGBColor>> {
            val array = Array(2) { Array(2) { RGBColor.BW } }
            if (pattern == GRAYSCALE) {
                array[0][0] = RGBColor.BW
                array[1][0] = RGBColor.BW
                array[0][1] = RGBColor.BW
                array[1][1] = RGBColor.BW
            } else {
                array[0][0] = RGBColor.valueOf(pattern[0].toString())
                array[1][0] = RGBColor.valueOf(pattern[1].toString())
                array[0][1] = RGBColor.valueOf(pattern[2].toString())
                array[1][1] = RGBColor.valueOf(pattern[3].toString())
            }
            return array
        }
    }
}