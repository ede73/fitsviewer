package fi.iki.ede.imagefuncs

/*
Gamma < 1: This makes dark areas lighter, which can be useful for enhancing details in shadows.
Gamma = 1: This means no change to the image.
Gamma > 1: This makes bright areas darker, increasing contrast in the midtones and highlights.
 */
fun rncColorStretch(
    colorNBits: Int,
    bitDepth: Int,
    gamma: Float,
    stretchRangePercent: ClosedFloatingPointRange<Float>
): Int {
    val maxValue: Double = ((1L shl bitDepth) - 1).toDouble()
    if (BuildConfig.DEBUG) {
        require(stretchRangePercent.endInclusive <= 1.0f && stretchRangePercent.start >= 0.0f,
            { "stretchRangePercent must be between 0 and 1 (0% to 100%)" })
        require(stretchRangePercent.endInclusive > stretchRangePercent.start,
            { "stretchRangePercent end must be > start" })
        require(colorNBits <= maxValue,
            { "Color value($colorNBits) cannot exceed bit depth($bitDepth max($maxValue))! Make sure to pass color component only" })
    }
    return contrastStretch(gammaAdjust(colorNBits, bitDepth, gamma), bitDepth, stretchRangePercent)
}