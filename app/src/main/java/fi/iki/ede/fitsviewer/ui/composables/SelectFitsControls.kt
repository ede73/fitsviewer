package fi.iki.ede.fitsviewer.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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

@Composable
fun SelectFitsControls(
    selectedImage: MutableState<Map.Entry<String, Int>>,
    images: Map<String, Int>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(modifier = Modifier.clickable { expanded = true }) {
            Text(selectedImage.value.key)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            images.forEach { entry ->
                DropdownMenuItem(
                    text = {
                        Text(entry.key)
                    },
                    onClick = {
                        selectedImage.value = entry
                        expanded = false
                    }
                )
            }
        }
    }
}
