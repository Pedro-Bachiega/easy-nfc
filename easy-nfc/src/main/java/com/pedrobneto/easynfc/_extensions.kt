package com.pedrobneto.easynfc

@OptIn(ExperimentalStdlibApi::class)
internal val Int.asByte: Byte
    get() = this.toHexString(format = HexFormat.UpperCase).toInt(radix = 16).toByte()

internal val Char.isHexChar: Boolean
    get() = this.lowercaseChar() in ('0'..'9') + ('a'..'f')

internal val String.asByteArray: ByteArray
    get() {
        val cleanedHexString = filter(Char::isHexChar)
        if (cleanedHexString.length % 2 > 0) return byteArrayOf()

        return cleanedHexString.zipWithNext()
            .filterIndexed { index, _ -> index % 2 == 0 }
            .map { "${it.first}${it.second}".toInt(radix = 16).toByte() }
            .toByteArray()
    }
