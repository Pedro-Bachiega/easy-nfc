package com.pedrobneto.easynfc.model

data class ApduCommandHeader(
    val clazz: Byte,
    val instruction: Byte,
    val parameter1: Byte,
    val parameter2: Byte,
) {
    companion object DefaultHeaders {
        val selectAid: ApduCommandHeader
            get() = ApduCommandHeader(
                clazz = 0x00,
                instruction = 0xA4.toByte(),
                parameter1 = 0x04,
                parameter2 = 0x00
            )

        val updateBinary: ApduCommandHeader
            get() = ApduCommandHeader(
                clazz = 0x00,
                instruction = 0xD6.toByte(),
                parameter1 = 0x00,
                parameter2 = 0x00
            )

        val writeBinary: ApduCommandHeader
            get() = ApduCommandHeader(
                clazz = 0x00,
                instruction = 0xD0.toByte(),
                parameter1 = 0x00,
                parameter2 = 0x00
            )

        fun fromHexString(hexString: String): ApduCommandHeader =
            hexString.encodeToByteArray().let {
                ApduCommandHeader(
                    clazz = it[0],
                    instruction = it[1],
                    parameter1 = it[2],
                    parameter2 = it[3]
                )
            }

        fun fromByteArray(byteArray: ByteArray): ApduCommandHeader =
            ApduCommandHeader(
                clazz = byteArray[0],
                instruction = byteArray[1],
                parameter1 = byteArray[2],
                parameter2 = byteArray[3]
            )
    }
}