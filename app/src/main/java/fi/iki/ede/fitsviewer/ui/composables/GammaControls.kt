package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun GammaControls(onValuesChange: (Float) -> Unit, start: Float) {
    var gamma by remember { mutableFloatStateOf(start) }
    Text("Gamma")
    Row {
        Text(text = "${gamma}")

        DebouncedSlider(value = gamma,
            valueRange = 0.01f..4f,
            steps = 50,
            onValueChange = {
                gamma = it
                onValuesChange(gamma)
            })
    }
}