package fi.iki.ede.fits.demosaicing

import kotlin.math.max
import kotlin.math.min

data class ColorStats(
    var min: Float = Float.MAX_VALUE,
    var max: Float = Float.MIN_VALUE,
    var sum: Float = 0.0f,
    var count: Int = 0
) {
    fun addValue(value: Float): Float {
        min = min(min, value)
        max = max(max, value)
        sum += value
        count++
        return value
    }

    fun getAverage(): Float {
        return if (count > 0) sum.toFloat() / count else Float.NaN
    }

    override fun toString(): String {
        return "min=$min, max=$max, sum=$sum, count=$count"
    }
}