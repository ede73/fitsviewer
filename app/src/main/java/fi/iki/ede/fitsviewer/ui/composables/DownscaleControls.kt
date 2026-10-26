package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import kotlin.math.round

@Composable
fun DownscaleControls(sizeDownscaling: MutableFloatState) {
    Text("Size scaling")
    Row {
        Text(text = "${sizeDownscaling.floatValue}")

        DebouncedSlider(value = sizeDownscaling.floatValue,
            valueRange = 1f..20f,
            steps = 20,
            onValueChange = {
                sizeDownscaling.floatValue = round(it)
            })
    }
}