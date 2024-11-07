@file:Suppress("FunctionName")

package com.pedrobneto.easynfc.model

import android.nfc.tech.IsoDep
import androidx.annotation.IntRange
import com.pedrobneto.easynfc.asByteArray
import com.pedrobneto.easynfc.asInt
import com.pedrobneto.easynfc.toByteArray

/**
 * A class to help you easily transfer data between NFC enabled devices.
 * Only use this constructor if you want to build the full content manually.
 *
 * @param content The content of the command
 */
class ApduCommand(val content: ByteArray) {

    /**
     * Create a command passing a [ByteArray] content.
     * Use this if you want full control of the data being sent.
     *
     * @param clazz The first [Byte] of the command - Defaults to 0x80
     * @param instruction The second [Byte] of the command - Defaults to 0x04
     * @param parameter1 The third [Byte] of the command - Defaults to 0x00
     * @param parameter2 The fourth [Byte] of the command - Defaults to 0x00
     * @param fullContent The content of the command
     */
    constructor(
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04,
        parameter1: Byte = 0x00,
        parameter2: Byte = 0x00,
        fullContent: ByteArray,
    ) : this(
        byteArrayOf(
            clazz,
            instruction,
            parameter1,
            parameter2,
            *fullContent,
        )
    )

    /**
     * Create a command passing a [ByteArray] content and a predetermined header.
     * Use this if you want full control of the data being sent.
     *
     * @param header The predetermined header for this command
     * @param fullContent The content of the command
     */
    constructor(
        header: ApduCommandHeader,
        fullContent: ByteArray,
    ) : this(
        clazz = header.clazz,
        instruction = header.instruction,
        parameter1 = header.parameter1,
        parameter2 = header.parameter2,
        fullContent = fullContent,
    )

    /**
     * Create a command passing a [ByteArray] content.
     *
     * @param clazz The first [Byte] of the command - Defaults to 0x80
     * @param instruction The second [Byte] of the command - Defaults to 0x04
     * @param parameter1 The third [Byte] of the command - Defaults to 0x00
     * @param parameter2 The fourth [Byte] of the command - Defaults to 0x00
     * @param content The content of the command
     * @param contentSize The length of the content
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04,
        parameter1: Byte = 0x00,
        parameter2: Byte = 0x00,
        content: ByteArray,
        contentSize: Int,
        contentSizeByteLength: Int = 1,
    ) : this(
        clazz = clazz,
        instruction = instruction,
        parameter1 = parameter1,
        parameter2 = parameter2,
        fullContent = contentSize.toByteArray(contentSizeByteLength) + content
    )

    /**
     * Create a command passing a [ByteArray] content and a predetermined header.
     *
     * @param header The predetermined header for this command
     * @param content The content of the command
     * @param contentSize The length of the content
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        header: ApduCommandHeader,
        content: ByteArray,
        contentSize: Int,
        contentSizeByteLength: Int = 1,
    ) : this(
        header = header,
        fullContent = contentSize.toByteArray(contentSizeByteLength) + content
    )

    /**
     * Create a command passing a [ByteArray] content.
     *
     * @param hexString The [Byte] string of the command header
     * @param content The content of the command
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        hexString: String,
        content: ByteArray,
        contentSize: Int,
        contentSizeByteLength: Int = 1,
    ) : this(
        header = ApduCommandHeader.from(hexString),
        content = content,
        contentSize = contentSize,
        contentSizeByteLength = contentSizeByteLength
    )

    /**
     * Returns the command's header.
     *
     * @return The command's header as a [ApduCommandHeader]
     */
    fun getHeader(): ApduCommandHeader = ApduCommandHeader.from(content)

    /**
     * Returns the command's data length.
     *
     * @param dataLengthByteQuantity The quantity of bytes representing the data length - Defaults to 1
     *
     * @return The command's data length as a [ByteArray]
     */
    fun getDataSize(@IntRange(from = 1) dataLengthByteQuantity: Int = 1): Int =
        if (dataLengthByteQuantity > 1) {
            content.sliceArray(4..4 + dataLengthByteQuantity - 1)
        } else {
            byteArrayOf(content[4])
        }.asInt

    /**
     * Returns the command's data.
     *
     * @param dataLengthByteQuantity The quantity of bytes representing the data length - Defaults to 1
     *
     * @return The command's data as a [ByteArray]
     */
    fun getData(@IntRange(from = 1) dataLengthByteQuantity: Int = 1): ByteArray {
        val length: Int = getDataSize(dataLengthByteQuantity)
        return content.sliceArray(4 + dataLengthByteQuantity..length)
    }

    /**
     * Returns the command's data as a [String].
     *
     * @param dataLengthByteQuantity The quantity of bytes representing the data length - Defaults to 1
     *
     * @return The command's data as a [String]
     */
    fun getDataAsString(dataLengthByteQuantity: Int = 1): String =
        getData(dataLengthByteQuantity).decodeToString()

    /**
     * Executes the given command on the given [IsoDep] tag.
     *
     * @param isoDep The [IsoDep] tag to execute the command on
     *
     * @return The tag's response as a [ByteArray]
     */
    fun executeOnTag(isoDep: IsoDep): ByteArray = isoDep.transceive(content)

    companion object {
        /**
         * Create a command passing a String content.
         *
         * @param clazz The first [Byte] of the command - Defaults to 0x80
         * @param instruction The second [Byte] of the command - Defaults to 0x04
         * @param parameter1 The third [Byte] of the command - Defaults to 0x00
         * @param parameter2 The fourth [Byte] of the command - Defaults to 0x00
         * @param content The content of the command
         * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
         */
        operator fun invoke(
            clazz: Byte = 0x80.toByte(),
            instruction: Byte = 0x04,
            parameter1: Byte = 0x00,
            parameter2: Byte = 0x00,
            content: String,
            contentSizeByteLength: Int = 1
        ): ApduCommand = content.encodeToByteArray().let {
            ApduCommand(
                clazz = clazz,
                instruction = instruction,
                parameter1 = parameter1,
                parameter2 = parameter2,
                content = it,
                contentSize = it.size,
                contentSizeByteLength = contentSizeByteLength
            )
        }

        /**
         * Create a command passing a [String] content and a predetermined header.
         *
         * @param header The predetermined header for this command
         * @param content The content of the command
         * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
         */
        operator fun invoke(
            header: ApduCommandHeader,
            content: String,
            contentSizeByteLength: Int = 1
        ): ApduCommand = content.encodeToByteArray().let {
            ApduCommand(
                header = header,
                content = it,
                contentSize = it.size,
                contentSizeByteLength = contentSizeByteLength
            )
        }

        /**
         * Create a command passing a [String] content.
         *
         * @param hexString The [Byte] String of the command header
         * @param content The content of the command
         * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
         */
        operator fun invoke(
            hexString: String,
            content: String,
            contentSizeByteLength: Int = 1
        ): ApduCommand = invoke(
            header = ApduCommandHeader.from(hexString),
            content = content,
            contentSizeByteLength = contentSizeByteLength
        )
    }
}

/**
 * Create a select aid command by passing a [ByteArray] aid.
 *
 * @param aid The application id as a [ByteArray]
 */
fun SelectAidCommand(aid: ByteArray): ApduCommand = ApduCommand(
    header = ApduCommandHeader.selectAid,
    content = aid,
    contentSize = aid.size
)

/**
 * Create a select aid command by passing a [String] aid.
 *
 * @param aid The application id as a [String]
 */
fun SelectAidCommand(aid: String): ApduCommand = SelectAidCommand(aid.asByteArray)

/**
 * Merges two commands, keeping the second command's header.
 * If the first command is null, returns the second command,
 *
 * @param other The other command to be combined with
 *
 * @return The combined commands as a [ApduCommand]
 */
operator fun ApduCommand?.plus(other: ApduCommand): ApduCommand {
    if (this == null) return other
    return ApduCommand(
        header = other.getHeader(),
        content = getData() + other.getData(),
        contentSize = getDataSize() + other.getDataSize()
    )
}
