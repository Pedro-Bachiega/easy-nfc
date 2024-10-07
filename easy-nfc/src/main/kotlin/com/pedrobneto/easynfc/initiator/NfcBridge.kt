package com.pedrobneto.easynfc.initiator

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.SelectAidCommand
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
        val result = SelectAidCommand(aid).executeOnTag(isoDep)
        if (!result.isSuccess) error("Could not select aid ${aid.contentToString()}")
    }

    /**
     * Send a command to the NFC device.
     *
     * @param command The command to be executed
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        command: ApduCommand,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = onConnection(aid = aid, transformResult = transformResult) {
        val result = command.executeOnTag(isoDep)
        when {
            result.isSuccess -> result
            acceptEmpty && result.isEmpty() -> byteArrayOf()
            else -> error("Command failed")
        }
    }

    /**
     * Send a command to the NFC device.
     *
     * @param command The command to be executed
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        command: ApduCommand,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ) = sendData(command = command, aid = aid, acceptEmpty = acceptEmpty, transformResult = {})

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        content: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = sendData(
        command = ApduCommand(content = content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = transformResult
    )

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        content: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ) = sendData(
        content = content,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = {}
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     */
    fun <T : Any> sendData(
        content: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (ByteArray) -> T
    ) = sendData(
        command = ApduCommand(content = content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = transformResult
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     */
    fun sendData(
        content: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false
    ) = sendData(
        content = content,
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
