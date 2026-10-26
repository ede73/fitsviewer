package fi.iki.ede.fitsviewer

import android.net.Uri
import android.os.Bundle
import java.io.ByteArrayOutputStream
import java.io.InputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import fi.iki.ede.fits.shared.FitsDocument
import fi.iki.ede.fits.shared.FitsViewer
import fi.iki.ede.fitsviewer.ui.theme.FITSViewerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var bytes by remember { mutableStateOf(context.resources.openRawResource(R.raw.m31).use { it.readBytes() }) }
            var name by remember { mutableStateOf("m31.fits (sample)") }
            val scope = rememberCoroutineScope()
            val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                if (uri != null) scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)!!.use { stream ->
                                stream.readBounded(FitsDocument.MAX_FILE_BYTES)
                            }
                        }
                    }.onSuccess { selected -> bytes = selected; name = uri.lastPathSegment ?: "local FITS" }
                }
            }
            FITSViewerTheme {
                FitsViewer(bytes, name, onRequestOpen = { openDocument.launch(arrayOf("*/*")) })
            }
        }
    }
}

private fun InputStream.readBounded(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(64 * 1024)
    var total = 0
    while (true) {
        val count = read(buffer, 0, minOf(buffer.size, maxBytes + 1 - total))
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "FITS file exceeds the 128 MiB viewer limit" }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}
