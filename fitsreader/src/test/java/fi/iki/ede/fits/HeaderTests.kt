/*
 * Copyright (c) Erkka Marjakangas 2023 . All rights reserved.
 *
 * See LICENCE for further details.
 */

package fi.iki.ede.fits

import fi.iki.ede.fits.fits.stream.FITSInputStream
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class HeaderTests {

    @Test
    fun loader1() {
        val fits = FITSInputStream(makeFITSHeader(FITS1), skipRasterData = true)
        assertEquals(true, fits.fitsHeader.headerValues["SIMPLE"]?.getBoolean())
        assertEquals("16", fits.fitsHeader.headerValues["BITPIX"]?.value)
        assertEquals("3C405", fits.fitsHeader.headerValues["OBJECT"]?.getString())
        assertEquals(2.18688869476, fits.fitsHeader.headerValues["BZERO"]?.getDouble())
        assertEquals("'14/07/83'", fits.fitsHeader.headerValues["DATE-MAP"]?.value)
        assertEquals("'19/08/83'", fits.fitsHeader.headerValues["DATE"]?.value)
        assertEquals(
            "UVLOD\n" +
                    "UVLOD OUTNAME='CYGA       ' OUTCLASS='XY    '\n" +
                    "UVLOD OUTSEQ=    1    OUTDISK= 3\n" +
                    "AIPS   IMNAME='CYGA   '      IMCLASS='IMAP  ' IMSEQ=   1\n" +
                    "AIPS   USERNO=   76", fits.fitsHeader.headerValues["HISTORY"]?.value
        )
    }

    @Test
    fun loader2() {
        val fits = FITSInputStream(makeFITSHeader(FITS2), true)
        assertEquals("T", fits.fitsHeader.headerValues["SIMPLE"]?.value)
        assertEquals(-2147483648, fits.fitsHeader.headerValues["BLANK"]?.getInt())
    }

    // Convert static linefeed split example input to actual FITS header input stream
    private fun makeFITSHeader(exampleInput: String): InputStream {
        val fitsHeaderBlob =
            exampleInput.trim().lines().joinToString(separator = "") { it.padEnd(80) }
        // Ok - clearly this header section is not 2880 (as one specification said)
        // assertEquals(2880, fitsHeaderBlob.length)
        return ByteArrayInputStream(fitsHeaderBlob.toByteArray())
    }

    companion object {
        // FITS header examples from https://archive.stsci.edu/fits/users_guide/node100.html#SECTION00900000000000000000

        private const val FITS1 = """
SIMPLE  =                    T /
BITPIX  =                   16 /
NAXIS   =                    4 /
NAXIS1  =                 2048 /
NAXIS2  =                 1024 /
NAXIS3  =                    1 /
NAXIS4  =                    1 /
EXTEND  =                    T / TABLES FOLLOWING MAIN IMAGE
BLOCKED =                    T / FILE MAY BE BLOCKED
OBJECT  = '3C405   '           / SOURCE NAME
TELESCOP= '        '           /
INSTRUME= '        '           /
OBSERVER= 'PERL    '           /
DATE-OBS= '27/10/82'           /OBSERVATION START DATE DD/MM/YY
DATE-MAP= '14/07/83'           /DATE OF LAST PROCESSING DD/MM/YY
BSCALE  =    7.04625720812E-05 /REAL = FITS_VALUE * BSCALE + BZERO 
BZERO   =    2.18688869476E+00 /
BUNIT   = 'JY/BEAM '           /UNITS OF FLUX
EPOCH   =      1.950000000E+03 /EPOCH OF RA DEC
DATAMAX =      4.495524406E+00 /MAX PIXEL VALUE
DATAMIN =     -1.217470840E-01 /MIN PIXEL VALUE
CTYPE1  = 'RA---SIN'           /
CRVAL1  =    2.99435165226E+02 /
CDELT1  =     -4.166666986E-05 /
CRPIX1  =      1.024000000E+03 /
CROTA1  =      0.000000000E+00 /
CTYPE2  = 'DEC--SIN'           /
CRVAL2  =    4.05961940065E+01 /
CDELT2  =      4.166666986E-05 /
CRPIX2  =      5.130000000E+02 /
CROTA2  =      0.000000000E+00 /
CTYPE3  = 'FREQ    '           /
CRVAL3  =    4.86635000000E+09 /
CDELT3  =      1.250000000E+07 /
CRPIX3  =      1.000000000E+00 /
CROTA3  =      0.000000000E+00 /
CTYPE4  = 'STOKES  '           /
CRVAL4  =    1.00000000000E+00 /
CDELT4  =      1.000000000E+00 /
CRPIX4  =      1.000000000E+00 /
CROTA4  =      0.000000000E+00 /
HISTORY UVLOD /DATA BASE CREATED BY USER 76 AT 14-JUL-1983`10:17:08
HISTORY UVLOD OUTNAME='CYGA       ' OUTCLASS='XY    '
HISTORY UVLOD OUTSEQ=    1    OUTDISK= 3
ORIGIN  = 'AIPSNRAO VLA VAX3  '        /
DATE    = '19/08/83'                   / FILE WRITTEN ON DD/MM/YY
HISTORY AIPS   IMNAME='CYGA   '      IMCLASS='IMAP  ' IMSEQ=   1 /
HISTORY AIPS   USERNO=   76            /
END            
        """

        private const val FITS2 = """
SIMPLE  =                    T  /  FITS TAPE WRITTEN AT KPNO, 04/18/80. 
BITPIX  =                   32  /  4-BYTE, 2-S COMPLEMENT INTEGERS. 
NAXIS   =                    2  /  NUMBER OF AXES. 
NAXIS1  =                  256  /  NUMBER OF PIXELS PER ROW. 
NAXIS2  =                  256  /  NUMBER OF ROWS. 
BSCALE  =          1.00000E-06  /  PHYSICAL=INTEGER*BSCALE+BZERO. 
BZERO   =                   0.  / 
BLANK   =          -2147483648  /  INTEGER VALUE FOR BLANK PIXEL. 

IPPS-RF = 'D      /013'         /  RASTER LFN/RASTER ORDINAL. 
IPPS-ID = 'N4486 NUCLEUS AND JET 4350 [9 BAD PIXELS BLANKED]        ' / 
IPPS-B/P=                   30  /  BITS/PIXEL OF IPPS RASTER. 
IPPS-MIN=         3.700018E-02  /  MINIMUM VALUE IN RASTER. 
IPPS-MAX=              1.19825  /  MAXIMUM VALUE IN RASTER. 
ORIGIN  = 'KPNO    -- WFITS OF 04/17/80.' / 
DATE    = '18/04/80'                             / 
TIME    = '10.11.54' 
JOBNAME = 'DCWGF27 ' 
COMMENT   THIS FILE TESTS THE 32-BIT PIXEL FORMAT. 
COMMENT   IT ALSO TESTS THE BLANK PIXEL CONVENTION. THERE ARE 9 BLANKS IN THIS 
COMMENT   IMAGE. THEY ARE A DETECTOR BLEMISH AREA. NOT ALL OF THE BLEMISH HAS
COMMENT   BEEN BLANKED. 

OBJECT  = 'NGC4486  =M87 AND JET' / 
TELESCOP= 'MAYALL4M'           / MAYALL 4-METER TELESCOPE AT KITT PEAK.
INSTRUME= 'VIDEOCAM - MAYALL 4M CASSEGRAIN' / 

COMMENT   THE KPNO "VIDEO CAMERA" IS AN INTEGRATING DIGITAL TELEVISION SYSTEM
COMMENT   WHICH USES AN  RCA ISIT VIDICON TUBE AND ACCUMULATES THE IMAGE IN 
COMMENT   A 256-SQUARE 20-BIT RAM DURING THE INTEGRATION. 
COMMENT   THIS IMAGE HAS BEEN CORRECTED FOR THE DARK CURRENT AND GAIN VARIATIONS
COMMENT   ACROSS THE FIELD. I.E., THE DARK IMAGE WAS SUBTRACTED, AND THE FLAT 
COMMENT   FIELD IMAGE WAS DIVIDED INTO THE RAW IMAGE. BECAUSE THE FLAT WAS NOT 
COMMENT   NORMALIZED IN THIS CASE, THE RESULTING RATIO IMAGE IS OF ORDER UNITY.
COMMENT   THIS IS A QUANTUM NOISE LIMITED IMAGE IN RELATIVE INTENSITY UNITS. 
COMMENT   THE GEOMETRY OF THIS IMAGE HAS NOT BEEN CORRECTED FOR THE PINCUSHION 
COMMENT   DISTORTION OF THE VIDEO CAMERA. 

END            
        """

        private const val FITS3 = """
SIMPLE  =                    T / STANDARD FITS FORMAT (REV OCT 1981)
BITPIX  =                    8 / CHARACTER INFORMATION
NAXIS   =                    0 / NO IMAGE DATA ARRAY PRESENT  
EXTEND  =                    T / THERE IS AN EXTENSION
ORIGIN  = 'ESO     '           / EUROPEAN SOUTHERN OBSERVATORY
OBJECT  = 'SNG - CAT.     '    / THE IDENTIFIER
DATE    = '27/ 5/84'           / DATE THIS TAPE WRITTEN DD/MM/YY  
END
XTENSION= 'TABLE   '           / TABLE EXTENSION
BITPIX  =                    8 / CHARACTER INFORMATION
NAXIS   =                    2 / SIMPLE 2-D MATRIX 
NAXIS1  =                   98 / NO. OF CHARACTERS PER ROW
NAXIS2  =                   10 / NO. OF ROWS
PCOUNT  =                    0 / RANDOM PARAMETER COUNT
GCOUNT  =                    1 / GROUP COUNT
TFIELDS =                    7 / NO. OF FIELDS PER ROW
TTYPE1  = 'IDEN.   '           / NAME OF ROW
TBCOL1  =                    1 / BEGINNING COLUMN OF THE FIELD
TFORM1  = 'E14.7   '           / FORMAT
TNULL1  = '        '           / NULL VALUE
TTYPE2  = 'RA      '           / NAME OF ROW
TBCOL2  =                   15 / BEGINNING COLUMN OF THE FIELD
TFORM2  = 'E14.7   '           / FORMAT
TNULL2  = '        '           / NULL VALUE  
TTYPE3  = 'DEC     '           / NAME OF ROW
TBCOL3  =                   29 / BEGINNING COLUMN OF THE FIELD
TFORM3  = 'E14.7   '           / FORMAT 
TNULL3  = '        '           / NULL NAME
TTYPE4  = 'TYPE    '           / NAME OF ROW
TBCOL4  =                   43 / BEGINNING COLUMN OF THE FIELD
TFORM4  = 'E14.7   '           / FORMAT
TNULL4  = '        '           / NULL VALUE
TTYPE5  = 'D25     '           / NAME OF ROW
TBCOL5  =                   57 / BEGINNING OF COLUMN OF THE FIELD
TFORM5  = 'E14.7   '           / FORMAT
TNULL5  = '        '           / NULL VALUE
TTYPE6  = 'INCL.   '           / NAME OF ROW
TBCOL6  =                   71 / BEGINNING COLUMN OF THE FIELD
TFORM6  = 'E14.7   '           / FORMAT
TNULL6  = '        '           / NULL VALUE
TTYPE7  = 'RV      '           / NAME OF ROW
TBCOL7  =                   85 / BEGINNING COLUMN OF THE FIELD
TFORM7  = 'E14.7   '           / FORMAT
TNULL7  = '        '           / NULL VALUE
END
        """
    }
}