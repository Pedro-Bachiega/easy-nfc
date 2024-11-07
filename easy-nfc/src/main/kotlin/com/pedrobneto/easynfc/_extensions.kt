package com.pedrobneto.easynfc

import androidx.annotation.IntRange
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
 * Converts the given [ByteArray] to an [Int].
 */
val ByteArray.asInt: Int
    get() = when {
        size == Byte.SIZE_BYTES -> first().toInt()
        size in Short.SIZE_BYTES until Int.SIZE_BYTES -> ByteBuffer.wrap(this).getShort().toInt()
        size >= Int.SIZE_BYTES -> ByteBuffer.wrap(this).getInt()
        else -> error("ByteArray is empty")
    }

/**
 * Converts the given [Int] to a [ByteArray] of the given size.
 *
 * Examples:
 * 0.toByteArray(1) -> `[0x00]`
 * 300.toByteArray(2) -> `[0x01, 0x2C]`
 * 300.toByteArray(3) -> `[0x00, 0x01, 0x2C]`
 * 300.toByteArray(1) -> throws IllegalStateException because 300 converted to a byte array
 * exceeds the given max byte size
 */
fun Int.toByteArray(@IntRange(from = 1) size: Int = Int.SIZE_BYTES): ByteArray = when {
    size == Byte.SIZE_BYTES -> byteArrayOf(this.toByte())
    size in Short.SIZE_BYTES until Int.SIZE_BYTES -> ByteBuffer.allocate(size)
        .order(ByteOrder.BIG_ENDIAN).putShort(this.toShort()).array()

    size >= Int.SIZE_BYTES -> ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN).putInt(this)
        .array()

    else -> error("ByteArray size '$size' can not be less than 1")
}
