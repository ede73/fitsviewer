package fi.iki.ede.fitsviewer.imagefuncsupport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fi.iki.ede.fitsviewer.ui.composables.GammaControls
import fi.iki.ede.fitsviewer.ui.composables.RangeSliderControls
import fi.iki.ede.imagefuncs.BuildConfig
import fi.iki.ede.imagefuncs.rncColorStretch

@Composable
fun RncColorStretchAlgorithm(selectedAlgorithm: MutableState<ContrastAlgorithm>) {
    var gamma by remember { mutableFloatStateOf(1.46f) }
    var l by remember { mutableFloatStateOf(0f) }
    var h by remember { mutableFloatStateOf(0.8f) }

    fun update(g: Float, low: Float, high: Float) {
        selectedAlgorithm.value = { colorNBits: Int, colorDepth: Int ->
            if (BuildConfig.DEBUG) {
                require(
                    low <= high,
                    { "lowValuePercentage must be < highValuePercentage" })
            }
            rncColorStretch(
                colorNBits,
                colorDepth,
                g,
                low..high
            )
        }
    }
    GammaControls({
        gamma = it
        update(gamma, l, h)
    }, gamma)

    RangeSliderControls(l, h, 0f..1.0f) { low, high ->
        l = low
        h = high
        update(gamma, l, h)
    }
}

