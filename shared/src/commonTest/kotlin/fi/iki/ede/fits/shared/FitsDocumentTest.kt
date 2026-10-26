package fi.iki.ede.fits.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FitsDocumentTest {
    @Test
    fun parsesAndRendersFromTheOriginalSamples() {
        val bytes = tinyInt16Fits()
        val image = FitsDocument.parse(bytes)

        assertEquals(2, image.metadata.width)
        assertEquals(2, image.metadata.height)
        assertEquals(3000.0, image.pixel(0, 1))
        val rendered = image.render(0, 0, 2, 2, 2, 2,
            StretchSettings(algorithm = StretchAlgorithm.LINEAR, high = 1f))
        assertEquals(4, rendered.size)
        assertTrue(rendered.all { it ushr 24 == 0xff })
        assertTrue(rendered[3] > rendered[0])
    }

    private fun tinyInt16Fits(): ByteArray {
        val cards = listOf(
            "SIMPLE  =                    T",
            "BITPIX  =                   16",
            "NAXIS   =                    2",
            "NAXIS1  =                    2",
            "NAXIS2  =                    2",
            "END"
        ).joinToString("") { it.padEnd(80) }
        val header = cards.padEnd(2880).encodeToByteArray()
        val samples = byteArrayOf(0x03, 0xe8.toByte(), 0x07, 0xd0.toByte(), 0x0b, 0xb8.toByte(), 0x0f, 0xa0.toByte())
        return header + samples
    }
}
