package fi.iki.ede.fits.fits.stream

import fi.iki.ede.fits.fits.parsing.FITSHeaderDataUnit
import java.io.InputStream

class TrackingInputStream(private val inputStream: InputStream) {
    //}: InputStream() {
    private var position: Long = 0

    fun read(): Int {
        val byte = inputStream.read()
        if (byte != -1) {
            position++
        }
        return byte
    }

    fun read(b: ByteArray?): Int {
        val bytesRead = inputStream.read(b)
        position += bytesRead
        return bytesRead
    }

    fun read(b: ByteArray, off: Int, len: Int): Int {
        val bytesRead = inputStream.read(b, off, len)
        if (bytesRead != -1) {
            position += bytesRead
        }
        return bytesRead
    }

    fun getPosition(): Long {
        return position
    }

    fun skipToDefaultBlock(): Long {
        val reminder = getPosition() % FITSHeaderDataUnit.DEFAULT_HDU_SIZE
        if (reminder == 0L) return 0L
        val toSkip = FITSHeaderDataUnit.DEFAULT_HDU_SIZE - reminder
        return skipFully(toSkip)
    }

    private fun skipFully(bytesToSkip: Long): Long {
        var remaining = bytesToSkip
        var skipped: Long

        while (remaining > 0) {
            skipped = inputStream.skip(remaining)
            if (skipped <= 0) {
                break
            }
            remaining -= skipped
        }
        position += (bytesToSkip - remaining)
        return bytesToSkip - remaining
    }
}