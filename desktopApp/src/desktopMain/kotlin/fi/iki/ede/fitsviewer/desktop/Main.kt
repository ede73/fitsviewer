package fi.iki.ede.fitsviewer.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import fi.iki.ede.fits.shared.FitsDocument
import fi.iki.ede.fits.shared.FitsViewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Files
import javax.swing.JFileChooser

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "FITS Viewer", state = rememberWindowState(width = 1200.dp, height = 900.dp)) {
        var bytes by remember { mutableStateOf<ByteArray?>(null) }
        var name by remember { mutableStateOf("No FITS file selected") }
        val scope = rememberCoroutineScope()
        FitsViewer(bytes, name, onRequestOpen = {
            val chooser = JFileChooser()
            chooser.dialogTitle = "Open a FITS image"
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = chooser.selectedFile
                name = file.name
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        require(Files.size(file.toPath()) <= FitsDocument.MAX_FILE_BYTES) { "FITS file exceeds the 128 MiB viewer limit" }
                        Files.readAllBytes(file.toPath())
                    }
                        .onSuccess { bytes = it }
                        .onFailure { name = "Could not open ${file.name}: ${it.message}" }
                }
            }
        })
    }
}
