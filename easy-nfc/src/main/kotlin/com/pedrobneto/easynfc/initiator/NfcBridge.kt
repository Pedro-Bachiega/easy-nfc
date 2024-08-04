package com.pedrobneto.easynfc.initiator

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.pedrobneto.easynfc.asByte
import kotlinx.coroutines.CoroutineScope

class NfcBridge private constructor(
    private val aid: ByteArray,
    private val isoDep: IsoDep,
    private val scope: CoroutineScope
) {

    private val ByteArray?.isSuccess: Boolean
        get() = this == null ||
                (this.size >= 2 && this[size - 2] == 0x90.toByte() && this[size - 1] == 0x00.toByte())

    internal constructor(aid: ByteArray, tag: Tag, scope: CoroutineScope) : this(
        aid = aid,
        isoDep = IsoDep.get(tag),
        scope = scope
    )

    private fun selectAid(aid: ByteArray) {
        val selectAidCommand = createCommand(
            clazz = 0x00.toByte(),
            instruction = 0xA4.toByte(),
            parameter1 = 0x04.toByte(),
            content = aid
        )

        val result = isoDep.transceive(selectAidCommand)
        if (!result.isSuccess) error("Could not select aid ${aid.contentToString()}")
    }

    /**
     * Create a command passing a ByteArray content.
     *
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command - Defaults to 00
     * @param parameter2 The fourth byte of the command - Defaults to 00
     * @param content The content of the command
     */
    fun createCommand(
        clazz: Byte,
        instruction: Byte,
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        content: ByteArray,
    ) = byteArrayOf(
        clazz,
        instruction,
        parameter1,
        parameter2,
        content.size.asByte,
        *content
    )

    /**
     * Create a command passing a String content.
     *
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command - Defaults to 00
     * @param parameter2 The fourth byte of the command - Defaults to 00
     * @param content The content of the command
     */
    fun createCommand(
        clazz: Byte,
        instruction: Byte,
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        content: String,
    ) = createCommand(
        clazz = clazz,
        instruction = instruction,
        parameter1 = parameter1,
        parameter2 = parameter2,
        content = content.encodeToByteArray()
    )

    /**
     * Create a command passing a ByteArray content.
     *
     * @param byteString The byte string of the command
     * @param content The content of the command
     */
    fun createCommand(byteString: String, content: ByteArray): ByteArray {
        val byteData = byteString.encodeToByteArray()
        val clazz = byteData[0]
        val instruction = byteData[1]
        val parameter1 = byteData[2]
        val parameter2 = byteData[3]

        return createCommand(clazz, instruction, parameter1, parameter2, content)
    }

    /**
     * Create a command passing a String content.
     *
     * @param byteString The byte string of the command
     * @param content The content of the command
     */
    fun createCommand(byteString: String, content: String) =
        createCommand(byteString, content.encodeToByteArray())

    /**
     * Send a command to the NFC device.
     *
     * @param command The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        command: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = onConnection(aid = aid, transformResult = transformResult) {
        val result = it.transceive(command)
        when {
            result.isSuccess -> result
            acceptEmpty && result.isEmpty() -> byteArrayOf()
            else -> error("Command failed")
        }
    }

    /**
     * Send a command to the NFC device.
     *
     * @param command The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        command: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ) = sendData(command = command, aid = aid, acceptEmpty = acceptEmpty, transformResult = {})

    /**
     * Send a command to the NFC device.
     *
     * @param command The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        command: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = sendData(
        command = command.encodeToByteArray(),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = transformResult
    )

    /**
     * Send a command to the NFC device.
     *
     * @param command The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        command: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false
    ) = sendData(
        command = command.encodeToByteArray(),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = {}
    )

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param content The content of the command
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command
     * @param parameter2 The fourth byte of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        content: ByteArray,
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = sendData(
        command = createCommand(clazz, instruction, parameter1, parameter2, content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = transformResult
    )

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param content The content of the command
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command
     * @param parameter2 The fourth byte of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        content: ByteArray,
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ) = sendData(
        command = createCommand(clazz, instruction, parameter1, parameter2, content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = {}
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param content The content of the command
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command
     * @param parameter2 The fourth byte of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        content: String,
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = sendData(
        command = createCommand(clazz, instruction, parameter1, parameter2, content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = transformResult
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param content The content of the command
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command
     * @param parameter2 The fourth byte of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        content: String,
        clazz: Byte = 0x80.toByte(),
        instruction: Byte = 0x04.toByte(),
        parameter1: Byte = 0x00.toByte(),
        parameter2: Byte = 0x00.toByte(),
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false
    ) = sendData(
        command = createCommand(clazz, instruction, parameter1, parameter2, content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = {}
    )

    private fun <T> onConnection(
        aid: ByteArray,
        transformResult: (ByteArray) -> T,
        func: (IsoDep) -> ByteArray
    ) = NfcDataStream<T>(initialValue = NfcResult(status = NfcDataStream.Status.CONNECTING))
        .connect(scope, isoDep, { selectAid(aid) }, transformResult, func)
}
