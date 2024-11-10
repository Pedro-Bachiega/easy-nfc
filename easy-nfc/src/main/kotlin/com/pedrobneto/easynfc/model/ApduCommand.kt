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
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
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
     * Create a command passing a [ByteArray] content and a header.
     * Use this if you want full control of the data being sent.
     *
     * @param header The header for this command
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
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        content: ByteArray,
        contentSize: Int = content.size,
        contentSizeByteLength: Int = 1,
    ) : this(
        clazz = clazz,
        instruction = instruction,
        parameter1 = parameter1,
        parameter2 = parameter2,
        fullContent = contentSize.toByteArray(contentSizeByteLength) + content
    )

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
    constructor(
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        content: String,
        contentSizeByteLength: Int = 1
    ) : this(
        clazz = clazz,
        instruction = instruction,
        parameter1 = parameter1,
        parameter2 = parameter2,
        content = content.encodeToByteArray(),
        contentSizeByteLength = contentSizeByteLength
    )

    /**
     * Create a command passing a [ByteArray] content and a header.
     *
     * @param header The header for this command
     * @param content The content of the command
     * @param contentSize The length of the content
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        header: ApduCommandHeader,
        content: ByteArray,
        contentSize: Int = content.size,
        contentSizeByteLength: Int = 1,
    ) : this(
        header = header,
        fullContent = contentSize.toByteArray(contentSizeByteLength) + content
    )

    /**
     * Create a command passing a [String] content and a header.
     *
     * @param header The header for this command
     * @param content The content of the command
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        header: ApduCommandHeader,
        content: String,
        contentSizeByteLength: Int = 1
    ) : this(
        header = header,
        content = content.encodeToByteArray(),
        contentSizeByteLength = contentSizeByteLength
    )

    /**
     * Create a command passing a [ByteArray] content.
     *
     * @param headerHexString The [Byte] string of the command header
     * @param content The content of the command
     * @param contentSize The length of the content
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        headerHexString: String,
        content: ByteArray,
        contentSize: Int = content.size,
        contentSizeByteLength: Int = 1,
    ) : this(
        header = ApduCommandHeader.from(headerHexString),
        content = content,
        contentSize = contentSize,
        contentSizeByteLength = contentSizeByteLength
    )

    /**
     * Create a command passing a [String] content.
     *
     * @param headerHexString The [Byte] String of the command header
     * @param content The content of the command
     * @param contentSizeByteLength The quantity of bytes representing the data length - Defaults to 1
     */
    constructor(
        headerHexString: String,
        content: String,
        contentSizeByteLength: Int = 1
    ) : this(
        headerHexString = headerHexString,
        content = content.encodeToByteArray(),
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
    fun getDataSize(@IntRange(from = 1) dataLengthByteQuantity: Int = 1): Int {
        val lengthOffset = 4
        return if (dataLengthByteQuantity > 1) {
            content.sliceArray(lengthOffset..<lengthOffset + dataLengthByteQuantity)
        } else {
            byteArrayOf(content[lengthOffset])
        }.asInt
    }

    /**
     * Returns the command's data.
     *
     * @param dataLengthByteQuantity The quantity of bytes representing the data length - Defaults to 1
     *
     * @return The command's data as a [ByteArray]
     */
    fun getData(@IntRange(from = 1) dataLengthByteQuantity: Int = 1): ByteArray {
        val length: Int = getDataSize(dataLengthByteQuantity)
        val offset = 4 + dataLengthByteQuantity
        return content.sliceArray(offset..<offset + length)
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

/**
 * Merges two commands, keeping the second command's header.
 * If the first command is null, returns the second command,
 *
 * @param other The other command to be combined with
 *
 * @return The combined commands as a [ApduCommand]
 */
fun ApduCommand?.plus(
    other: ApduCommand,
    @IntRange(from = 1) dataLengthByteQuantity: Int = 1
): ApduCommand {
    if (this == null) return other
    return ApduCommand(
        header = other.getHeader(),
        content = getData(dataLengthByteQuantity) + other.getData(dataLengthByteQuantity),
        contentSize = getDataSize(dataLengthByteQuantity) + other.getDataSize(dataLengthByteQuantity),
        contentSizeByteLength = dataLengthByteQuantity
    )
}
