package fi.iki.ede.fitsviewer.imagefuncsupport

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContrastStretchSelector(selectedAlgorithm: MutableState<ContrastAlgorithm>) {
    val algorithms: List<Pair<String, @Composable () -> Unit>> =
        listOf(
            Pair("RNC", { RncColorStretchAlgorithm(selectedAlgorithm) }),
            Pair("Contrast", { ContrastStretchAlgorithm(selectedAlgorithm) })
        )
    var expanded by remember { mutableStateOf(false) }
    var selectedMenuItem by remember {
        mutableStateOf<Pair<String, @Composable () -> Unit>>(
            algorithms.first()
        )
    }

    Box(
        modifier = Modifier
            .border(1.dp, Color.DarkGray)
            .padding(8.dp)
    ) {
        Column {
            Box(modifier = Modifier.border(1.dp, Color.DarkGray)) {
                Row(modifier = Modifier.clickable { expanded = true }) {
                    Text(selectedMenuItem.first)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    algorithms.forEach { algorithm ->
                        DropdownMenuItem(
                            text = {
                                Text(algorithm.first)
                            },
                            onClick = {
                                selectedMenuItem = algorithm
                                expanded = false
                            }
                        )
                    }
                }
            }
            selectedMenuItem.second()
        }
    }
}
