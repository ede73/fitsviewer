package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fi.iki.ede.fitsviewer.BuildConfig


@Composable
fun RangeSliderControls(
    startValue: Float,
    endValue: Float,
    visualValueRange: ClosedFloatingPointRange<Float>,
    onValuesChange: (Float, Float) -> Unit,
) {
    var lowValuePercentage by remember { mutableFloatStateOf(startValue) }
    var highValuePercentage by remember { mutableFloatStateOf(endValue) }

    if (BuildConfig.DEBUG) {
        require(visualValueRange.endInclusive >= visualValueRange.start &&
                visualValueRange.start >= 0.0f &&
                visualValueRange.endInclusive <= 1.0f,
            { "visualValueRange must be between (0% to 100%)" })
    }
    Text("Stretch percentile (kinda)")
    Row {
        Text(text = "${(lowValuePercentage * 100.0f).toInt()}%-${(highValuePercentage * 100.0f).toInt()}%")

        DebouncedRangeSlider(
            startValue = lowValuePercentage,
            endValue = highValuePercentage,
            onValuesChange = { low, high ->
                lowValuePercentage = low
                highValuePercentage = high
                onValuesChange(low, high)
            },
            valueRange = visualValueRange,
            steps = 50,
        )
    }
}