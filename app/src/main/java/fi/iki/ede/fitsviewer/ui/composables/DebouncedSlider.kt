package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun DebouncedSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    debounceTimeMillis: Long = 500L,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(value) }
    var debouncedValue by remember { mutableFloatStateOf(value) }

    LaunchedEffect(sliderValue) {
        snapshotFlow { sliderValue }
            .distinctUntilChanged()
            .collectLatest {
                delay(debounceTimeMillis)
                debouncedValue = it
            }
    }

    LaunchedEffect(debouncedValue) {
        onValueChange(debouncedValue)
    }

    Row {
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = valueRange,
            steps = steps,
            modifier = modifier
        )
    }
}