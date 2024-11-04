package com.pedrobneto.easynfc

import androidx.annotation.IntRange

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

        return cleanedHexString.chunked(2)
            .map { "${it[0]}${it[1]}".toInt(radix = 16).toByte() }
            .toByteArray()
    }

/**
 * Converts the given [Int] to a [ByteArray] of the given size.
 *
 * Examples:
 * 0.toByteArray(1) -> `[0x00]`
 * 300.toByteArray(1, 2) -> `[0x01, 0x2C]`
 * 300.toByteArray(1, 3) -> `[0x00, 0x01, 0x2C]`
 * 300.toByteArray(1) -> throws IllegalStateException because 300 converted to a byte array
 * exceeds the given max byte size
 */
@OptIn(ExperimentalStdlibApi::class)
@Throws(IllegalStateException::class)
fun Int.toByteArray(@IntRange(from = 1) vararg acceptableSizes: Int): ByteArray {
    val hexString = this.toHexString(
        format = HexFormat {
            upperCase = true
            number { removeLeadingZeros = true }
        }
    )

    val acceptableStringSizes = acceptableSizes.map { it * 2 }
    val minAcceptableStringSize = acceptableStringSizes.min()

    return when {
        hexString.length in acceptableStringSizes -> hexString.asByteArray

        hexString.length < minAcceptableStringSize -> {
            hexString.padStart(minAcceptableStringSize, '0').asByteArray
        }

        else -> {
            acceptableStringSizes.forEach {
                if (it > hexString.length) return hexString.padStart(it, '0').asByteArray
            }

            val maxAcceptableStringSize = acceptableStringSizes.max()
            error(
                "Content length must not exceed ${maxAcceptableStringSize / 2} bytes. " +
                        "Expected size: 0x00 ~ 0x${
                            String().padEnd(maxAcceptableStringSize, 'F')
                        }, " +
                        "actual size: 0x$hexString"
            )
        }
    }
}
