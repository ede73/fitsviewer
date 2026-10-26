package fi.iki.ede.imagefuncs

fun contrastStretch(
    colorNBits: Int,
    bitDepth: Int,
    stretchRangePercent: ClosedFloatingPointRange<Float>
): Int {
    val maxValue = (1L shl bitDepth) - 1
    if (BuildConfig.DEBUG) {
        require(stretchRangePercent.endInclusive <= 1.0f && stretchRangePercent.start >= 0.0f,
            { "stretchRangePercent must be between 0 and 1 (0% to 100%)" })
        require(stretchRangePercent.endInclusive > stretchRangePercent.start,
            { "stretchRangePercent end must be > start" })
        require(colorNBits <= maxValue,
            { "Color value($colorNBits) cannot exceed bit depth($bitDepth($maxValue))! Make sure to pass color component only" })
    }
    val minValue = 0
    val newMax = maxValue * stretchRangePercent.endInclusive
    val newMin = maxValue * stretchRangePercent.start
    val scaleFactor = (newMax - newMin) / (maxValue - minValue)
    return ((colorNBits - minValue) * scaleFactor + newMin).coerceIn(newMin..newMax)
        .toInt()
}

