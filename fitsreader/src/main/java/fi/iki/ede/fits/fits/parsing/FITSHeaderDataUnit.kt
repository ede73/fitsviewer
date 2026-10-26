package fi.iki.ede.fits.fits.parsing

import fi.iki.ede.fits.fits.stream.TrackingInputStream
import java.io.StringWriter

// https://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html
class FITSHeaderDataUnit(trackingInputStream: TrackingInputStream) {
    private val headers: List<FITSHeaderSection>
    val headerValues = HashMap<String, FITSHeaderSection>()

    // header is always 2880 bytes+
    init {
        var headerCount = 0
        val mandatoryHeaderParts = mutableListOf(
            // FITS Primary HDU
            FITSHeaderSection(trackingInputStream, "SIMPLE"), // req'd first kword
            // Range: -64,-32,8,16,32
            // Number of bits that represent a data value
            FITSHeaderSection(trackingInputStream, "BITPIX"),
            // non neg num, number of axis
            FITSHeaderSection(trackingInputStream, "NAXIS"),
        )
        headerCount = 3

        val axis = mandatoryHeaderParts[2].value.toInt()
        for (i in 1..axis) {
            headerCount++
            // For any naxisN, it indicates no data follow the header in the HDU
            mandatoryHeaderParts.add(FITSHeaderSection(trackingInputStream, "NAXIS${i}"))
        }

        val optionalHeaderParts = mutableListOf<FITSHeaderSection>()

        while (true) {
            headerCount++
            val header = FITSHeaderSection(trackingInputStream)
            if (header.name.isNotEmpty()) {
                optionalHeaderParts.add(header)
                if (header.name == "END") {
                    break
                }
            }
        }

        // EVEN if end came early :) We still need to read at least 2880
        // ie. next rows might be empties..
        // TODO: ALl fits records are 2880 bytes (TODO: Padded? TODO: Binary data units also?)
        for (i in headerCount until DEFAULT_HDU_SIZE / STANDARD_HEADER_SIZE) {
            val header = FITSHeaderSection(trackingInputStream)
            assert(header.name.isEmpty()) { "First HDU contains unexpected stuff" }
        }

        trackingInputStream.skipToDefaultBlock()

        headers = mandatoryHeaderParts + optionalHeaderParts
        for (header in headers) {
            if (headerValues.contains(header.name)) {
                println("Amend $header")
                val name = header.name
                headerValues[name] = FITSHeaderSection(
                    name,
                    headerValues[name]!!.value + "\n${header.value}",
                    headerValues[name]!!.comment + "\n${header.comment}"
                )
            } else {
                headerValues[header.name] = header
            }
        }
    }

    override fun toString() = StringWriter().apply {
        headers.forEach {
            appendLine(it.toString())
        }
    }.toString()

    companion object {
        const val DEFAULT_HDU_SIZE = 2880
        private const val STANDARD_HEADER_SIZE = 80
    }
}