package fi.iki.ede.imagefuncs

import org.junit.Assert.assertEquals
import org.junit.Test


class ContrastStretchTests {
    val Int.percent: Float
        get() = this / 100.0f

    @Test
    fun gammaTest() {
        ({ g: Float -> gammaAdjust(129, 8, g) }).let {
            assertEquals(129, it(1.0f))
            assertEquals(119, it(0.9f))
            assertEquals(137, it(1.1f))
        }
    }

    @Test
    fun rncColorStretch() {
        ({ g: Float, lowPass: ClosedFloatingPointRange<Float> ->
            rncColorStretch(
                129,
                8,
                g,
                lowPass
            )
        }).let { rncStretch ->
            assertEquals(129, rncStretch(1.0f, 0.percent..100.percent))
            assertEquals(119, rncStretch(0.9f, 0.percent..100.percent))
            assertEquals(116, rncStretch(1.0f, 0.percent..90.percent))

            assertEquals(131, rncStretch(1.0f, 2.percent..100.percent))
            assertEquals(121, rncStretch(0.9f, 2.percent..100.percent))
            assertEquals(118, rncStretch(1.0f, 2.percent..90.percent))
        }
    }

    @Test
    fun contrastStretch() {
        ({ lowPass: ClosedFloatingPointRange<Float> ->
            contrastStretch(
                129,
                8,
                lowPass
            )
        }).let { contrastStretch ->
            assertEquals(129, contrastStretch(0.percent..100.percent))
            assertEquals(116, contrastStretch(0.percent..90.percent))

            assertEquals(131, contrastStretch(2.percent..100.percent))
            assertEquals(118, contrastStretch(2.percent..90.percent))
        }
    }
}