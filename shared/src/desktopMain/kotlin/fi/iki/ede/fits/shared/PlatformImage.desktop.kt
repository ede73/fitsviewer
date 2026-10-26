package fi.iki.ede.fits.shared

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage

internal actual fun createPlatformImageBitmap(width: Int, height: Int, argbPixels: IntArray): ImageBitmap {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, width, height, argbPixels, 0, width)
    return image.toComposeImageBitmap()
}
