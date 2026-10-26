package fi.iki.ede.fitsviewer.imagefuncsupport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import fi.iki.ede.fitsviewer.ui.composables.RangeSliderControls
import fi.iki.ede.imagefuncs.contrastStretch

@Composable
fun ContrastStretchAlgorithm(selectedAlgorithm: MutableState<ContrastAlgorithm>) {
    RangeSliderControls(
        0.1f,
        0.8f,
        0.0f..1.0f
    ) { low, high ->
        selectedAlgorithm.value = { colorNBits, bitDepth ->
            contrastStretch(
                colorNBits,
                bitDepth,
                low..high,
            )
        }
    }
}
