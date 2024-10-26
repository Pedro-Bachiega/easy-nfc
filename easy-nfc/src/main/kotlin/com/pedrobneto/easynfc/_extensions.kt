package com.pedrobneto.easynfc

/**
 * Creates a [ByteArray] from the given [String].
 * If the given [String] length is not an even number, a 0 will be added to the start.
 *
 * Examples:
 * "0xABCDEF" -> `[0xAB, 0xCD, 0xEF]`
 * "ABCDEF" -> `[0xAB, 0xCD, 0xEF]`
 */
val String.asByteArray: ByteArray
    get() {
        var cleanedHexString = replace("0x", "")
            .filter { it.lowercaseChar() in ('0'..'9') + ('a'..'f') }
        if (cleanedHexString.length % 2 > 0) {
            cleanedHexString = cleanedHexString.padStart(cleanedHexString.length + 1, '0')
        }

        return cleanedHexString.zipWithNext()
            .filterIndexed { index, _ -> index % 2 == 0 }
            .map { "${it.first}${it.second}".toInt(radix = 16).toByte() }
            .toByteArray()
    }

/**
 * Converts the given [Int] to a [ByteArray] of the given size.
 *
 * Examples:
 * 0.toByteArray(1) -> `[0x00]`
 * 300.toByteArray(2) -> `[0x01, 0x2C]`
 * 300.toByteArray(1) -> throws IllegalStateException because 300 converted to a byte array
 * exceeds the given max byte size
 */
@OptIn(ExperimentalStdlibApi::class)
@Throws(IllegalStateException::class)
fun Int.toByteArray(size: Int): ByteArray {
    val expectedStringSize = size * 2
    val hexString = this.toHexString(
        format = HexFormat {
            upperCase = true
            number { removeLeadingZeros = true }
        }
    )

    return when {
        hexString.length == expectedStringSize -> hexString.asByteArray
        hexString.length < expectedStringSize -> {
            hexString.padStart(expectedStringSize, '0').asByteArray
        }

        else -> error(
            "Content length must not exceed $size bytes. " +
                    "Expected size: 0x00 ~ ${String().padEnd(expectedStringSize, 'F')}, " +
                    "actual size: 0x$hexString"
        )
    }
}
