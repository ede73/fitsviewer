package fi.iki.ede.fits.fits.parsing

import fi.iki.ede.fits.fits.stream.TrackingInputStream

class FITSHeaderSection {
    val name: String
    val value: String
    val comment: String
    private val isAssignment: Boolean

    // only used by FITSHeader to combine header values (like in case with COMMENT or HISTORY
    // that may occur many times
    internal constructor(
        name: String,
        value: String,
        valueComment: String,
        isAssignment: Boolean = false
    ) {
        this.name = name
        this.value = value
        this.comment = valueComment
        this.isAssignment = isAssignment
    }

    constructor(trackingInputStream: TrackingInputStream, expectName: String = "") {
        val headerBlob = ByteArray(80)
        trackingInputStream.read(headerBlob)
        val headerString = String(headerBlob)
        if (headerString.trim().isEmpty()) {
            // There's nothing here
            name = ""
            value = ""
            comment = ""
            isAssignment = false
            return
        }
        // https://archive.stsci.edu/fits/users_guide/node19.html#SECTION00511000000000000000
        // only allowed chars are upper case, numbers, hyphen, understore
        // length max 8. Cannot have leading/trailing spaces
        val nameRegex = Regex("""^([A-Z0-9_-]{1,8})\s*(.*)$""")

        if (headerString.trim().startsWith("/")) {
            // ugly hack
            // Ok, apparently there can be lines that ONLY have a comment
            // e.g. "                 / GROUP PARAMETERS: OSS                                        "
            name = ""
            value = ""
            comment = headerString.trim()
            isAssignment = false
            return
        }
        val r = nameRegex.matchEntire(headerString)
        name = r!!.groups[1]!!.value.trim()

        val rest = r.groups[2]!!.value.trim()

        // https://archive.stsci.edu/fits/users_guide/node19.html#SECTION00511000000000000000
        // shorcutting specs, = should be in column 9 or 10, otherwise
        // cols 9-80 should be regarded as comment(except history)
        if (rest.startsWith("=")) {
            isAssignment = true
            val test = rest.trim().substring(1)
            val commentBegin = Regex("""\s/""")
            val match = commentBegin.split(test, 0)
            if (match.isNotEmpty()) {
                value = match[0].trim()
                comment = if (match.size > 1) {
                    match.subList(1, match.size - 1).joinToString("")
                } else {
                    ""
                }
            } else {
                value = test
                comment = ""
            }
        } else {
            isAssignment = false
            val dataComment = Regex("""^([^/]+)\s/(.*)""")
            val dataCommentMatch = dataComment.matchAt(rest, 0)
            if (dataCommentMatch != null) {
                value = dataCommentMatch.groups[1]?.value?.trim() ?: ""
                comment = dataCommentMatch.groups[2]?.value?.trim() ?: ""
            } else {
                value = rest
                comment = ""
            }
            assert(name.isNotEmpty()) { "FITS header name must be specified: input=($headerString)" }
        }
        if (expectName.isNotEmpty()) {
            assert(name == expectName.uppercase())
        }
        //println("Got ($name) => ($value) #$comment")
    }

    fun getString(): String {
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.lastIndex - 1).trim()
        }
        return value
    }

    override fun toString() =
        "Header '${name}' ${value}, #${comment} assignment? $isAssignment"

    fun getDouble() = value.toDouble()
    fun getInt() = value.toInt()
    fun getBoolean() = value == "T"
}