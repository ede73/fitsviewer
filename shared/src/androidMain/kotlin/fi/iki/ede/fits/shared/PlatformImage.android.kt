package fi.iki.ede.fits.shared

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun createPlatformImageBitmap(width: Int, height: Int, argbPixels: IntArray): ImageBitmap =
    Bitmap.createBitmap(argbPixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
