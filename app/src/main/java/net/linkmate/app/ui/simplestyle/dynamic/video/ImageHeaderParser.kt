package net.linkmate.app.ui.simplestyle.dynamic.video

import com.lxj.xpopup.enums.ImageType
import java.io.IOException
import java.io.InputStream

/**
 * Description: copy from Glide.
 * Create by lxj, at 2019/3/4
 */
object ImageHeaderParser {
    private const val GIF_HEADER = 0x474946
    private const val PNG_HEADER = -0x76afb1b9
    const val EXIF_MAGIC_NUMBER = 0xFFD8

    // WebP-related
    // "RIFF"
    private const val RIFF_HEADER = 0x52494646

    // "WEBP"
    private const val WEBP_HEADER = 0x57454250

    // "VP8" null.
    private const val VP8_HEADER = 0x56503800
    private const val VP8_HEADER_MASK = -0x100
    private const val VP8_HEADER_TYPE_MASK = 0x000000FF

    // 'X'
    private const val VP8_HEADER_TYPE_EXTENDED = 0x00000058

    // 'L'
    private const val VP8_HEADER_TYPE_LOSSLESS = 0x0000004C
    private const val WEBP_EXTENDED_ALPHA_FLAG = 1 shl 4
    private const val WEBP_LOSSLESS_ALPHA_FLAG = 1 shl 3

    @Throws(IOException::class)
    fun getImageType(`is`: InputStream): ImageType {
        val reader: Reader = StreamReader(`is`)
        val firstTwoBytes = reader.uInt16

        // JPEG.
        if (firstTwoBytes == EXIF_MAGIC_NUMBER) {
            return ImageType.JPEG
        }
        val firstFourBytes = firstTwoBytes shl 16 and -0x10000 or (reader.uInt16 and 0xFFFF)
        // PNG.
        if (firstFourBytes == PNG_HEADER) {
            // See: http://stackoverflow.com/questions/2057923/how-to-check-a-png-for-grayscale-alpha
            // -color-type
            reader.skip(25 - 4.toLong())
            val alpha = reader.byte
            // A RGB indexed PNG can also have transparency. Better safe than sorry!
            return if (alpha >= 3) ImageType.PNG_A else ImageType.PNG
        }

        // GIF from first 3 bytes.
        if (firstFourBytes shr 8 == GIF_HEADER) {
            return ImageType.GIF
        }

        // WebP (reads up to 21 bytes). See https://developers.google.com/speed/webp/docs/riff_container
        // for details.
        if (firstFourBytes != RIFF_HEADER) {
            return ImageType.UNKNOWN
        }
        // Bytes 4 - 7 contain length information. Skip these.
        reader.skip(4)
        val thirdFourBytes = reader.uInt16 shl 16 and -0x10000 or (reader.uInt16 and 0xFFFF)
        if (thirdFourBytes != WEBP_HEADER) {
            return ImageType.UNKNOWN
        }
        val fourthFourBytes = reader.uInt16 shl 16 and -0x10000 or (reader.uInt16 and 0xFFFF)
        if (fourthFourBytes and VP8_HEADER_MASK != VP8_HEADER) {
            return ImageType.UNKNOWN
        }
        if (fourthFourBytes and VP8_HEADER_TYPE_MASK == VP8_HEADER_TYPE_EXTENDED) {
            // Skip some more length bytes and check for transparency/alpha flag.
            reader.skip(4)
            return if (reader.byte and WEBP_EXTENDED_ALPHA_FLAG != 0) ImageType.WEBP_A else ImageType.WEBP
        }
        if (fourthFourBytes and VP8_HEADER_TYPE_MASK == VP8_HEADER_TYPE_LOSSLESS) {
            // See chromium.googlesource.com/webm/libwebp/+/master/doc/webp-lossless-bitstream-spec.txt
            // for more info.
            reader.skip(4)
            return if (reader.byte and WEBP_LOSSLESS_ALPHA_FLAG != 0) ImageType.WEBP_A else ImageType.WEBP
        }
        `is`.close()
        return ImageType.WEBP
    }

    private interface Reader {
        @get:Throws(IOException::class)
        val uInt16: Int

        @get:Throws(IOException::class)
        val uInt8: Short

        @Throws(IOException::class)
        fun skip(total: Long): Long

        @Throws(IOException::class)
        fun read(buffer: ByteArray?, byteCount: Int): Int

        @get:Throws(IOException::class)
        val byte: Int
    }

    private class StreamReader // Motorola / big endian byte order.
    internal constructor(private val `is`: InputStream) : Reader {
        @get:Throws(IOException::class)
        override val uInt16: Int
            get() = `is`.read() shl 8 and 0xFF00 or (`is`.read() and 0xFF)

        @get:Throws(IOException::class)
        override val uInt8: Short
            get() = (`is`.read() and 0xFF).toShort()

        @Throws(IOException::class)
        override fun skip(total: Long): Long {
            if (total < 0) {
                return 0
            }
            var toSkip = total
            while (toSkip > 0) {
                val skipped = `is`.skip(toSkip)
                if (skipped > 0) {
                    toSkip -= skipped
                } else {
                    // Skip has no specific contract as to what happens when you reach the end of
                    // the stream. To differentiate between temporarily not having more data and
                    // having finished the stream, we read a single byte when we fail to skip any
                    // amount of data.
                    val testEofByte = `is`.read()
                    if (testEofByte == -1) {
                        break
                    } else {
                        toSkip--
                    }
                }
            }
            return total - toSkip
        }

        @Throws(IOException::class)
        override fun read(buffer: ByteArray?, byteCount: Int): Int {
            var toRead = byteCount
            var read: Int = 0
            while (toRead > 0 && `is`.read(buffer, byteCount - toRead, toRead).also { read = it } != -1) {
                toRead -= read
            }
            return byteCount - toRead
        }

        @get:Throws(IOException::class)
        override val byte: Int
            get() = `is`.read()

    }
}
