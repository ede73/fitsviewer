package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState


@Composable
fun BrightnessControls(brightnessScaling: MutableFloatState) {
    Text("Brightness")
    Row {
        Text(text = "${brightnessScaling.floatValue}")

        DebouncedSlider(value = brightnessScaling.floatValue,
            valueRange = 0.1f..40f,
            steps = 50,
            onValueChange = {
                brightnessScaling.floatValue = it
            })
    }
}