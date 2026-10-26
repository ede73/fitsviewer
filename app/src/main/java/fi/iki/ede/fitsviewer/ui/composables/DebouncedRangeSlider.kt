package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fi.iki.ede.fitsviewer.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun DebouncedRangeSlider(
    startValue: Float,
    endValue: Float,
    onValuesChange: (Float, Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    debounceTimeMillis: Long = 500L,
    modifier: Modifier = Modifier
) {
    if (BuildConfig.DEBUG) {
        require(startValue <= endValue,
            { "Visual start range must be < end range" })
        require(valueRange.endInclusive >= valueRange.start,
            { "Visual start range must be < end range" })
        require(startValue in valueRange.start..valueRange.endInclusive,
            { "Start value must be >= start range <= end range" })
        require(endValue in valueRange.start..valueRange.endInclusive,
            { "End value must be >= start range <= end range" })
    }
    var startSliderValue by remember { mutableFloatStateOf(startValue) }
    var endSliderValue by remember { mutableFloatStateOf(endValue) }
    var debouncedStartValue by remember { mutableFloatStateOf(startValue) }
    var debouncedEndValue by remember { mutableFloatStateOf(endValue) }

    LaunchedEffect(startSliderValue) {
        snapshotFlow { startSliderValue }
            .distinctUntilChanged()
            .collectLatest {
                delay(debounceTimeMillis)
                debouncedStartValue = it
            }
    }

    LaunchedEffect(endSliderValue) {
        snapshotFlow { endSliderValue }
            .distinctUntilChanged()
            .collectLatest {
                delay(debounceTimeMillis)
                debouncedEndValue = it
            }
    }

    LaunchedEffect(debouncedStartValue, debouncedEndValue) {
        onValuesChange(debouncedStartValue, debouncedEndValue)
    }

    Row(modifier = modifier.padding(end = 8.dp)) {
        RangeSlider(
            value = startSliderValue..endSliderValue,
            onValueChange = { newValue ->
                if (startSliderValue != newValue.start) {
                    startSliderValue = newValue.start
                }
                if (endSliderValue != newValue.endInclusive) {
                    endSliderValue = newValue.endInclusive
                }
            },
            valueRange = valueRange.start..valueRange.endInclusive,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
    }
}
