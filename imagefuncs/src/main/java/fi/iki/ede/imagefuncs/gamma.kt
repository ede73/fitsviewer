package fi.iki.ede.imagefuncs

import kotlin.math.pow

fun gammaAdjust(colorNBits: Int, bitDepth: Int, gamma: Float) =
    ((1L shl bitDepth) - 1).let { maxValue ->
        ((colorNBits / maxValue.toDouble()).pow(1.0 / gamma) * maxValue).toInt()
            .coerceAtMost(maxValue.toInt())
    }
