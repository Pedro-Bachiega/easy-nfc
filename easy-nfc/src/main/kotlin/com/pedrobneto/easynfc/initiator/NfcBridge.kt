package com.pedrobneto.easynfc.initiator

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.pedrobneto.easynfc.asByteArray
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.SelectAidCommand
import kotlinx.coroutines.CoroutineScope

/**
 * A class designed to help you easily transfer data between NFC enabled devices.
 *
 * @param aid The AID (application id) of the target NFC device
 * @param isoDep The tag we are connecting to
 * @param scope The [CoroutineScope] the connection will be established in
 *
 * @see IsoDep.get
 */
class NfcBridge private constructor(
    private val aid: ByteArray,
    private val isoDep: IsoDep,
    private val scope: CoroutineScope
) {

    private val ByteArray?.isSuccess: Boolean
        get() = this == null ||
                (this.size >= 2 && this[size - 2] == 0x90.toByte() && this[size - 1] == 0x00.toByte())

    /**
     * A class designed to help you easily transfer data between NFC enabled devices.
     *
     * @param aid The AID (application id) of the target NFC device
     * @param tag The tag we are connecting to
     * @param scope The [CoroutineScope] the connection will be established in
     */
    constructor(aid: ByteArray, tag: Tag, scope: CoroutineScope) : this(
        aid = aid,
        isoDep = IsoDep.get(tag),
        scope = scope
    )

    /**
     * A class designed to help you easily transfer data between NFC enabled devices.
     *
     * @param aid The AID (application id) of the target NFC device
     * @param isoDep The tag we are connecting to
     * @param scope The [CoroutineScope] the connection will be established in
     */
    constructor(aid: String, isoDep: IsoDep, scope: CoroutineScope) : this(
        aid = aid.asByteArray,
        isoDep = isoDep,
        scope = scope
    )

    /**
     * A class designed to help you easily transfer data between NFC enabled devices.
     *
     * @param aid The AID (application id) of the target NFC device
     * @param tag The tag we are connecting to
     * @param scope The [CoroutineScope] the connection will be established in
     */
    constructor(aid: String, tag: Tag, scope: CoroutineScope) : this(
        aid = aid,
        isoDep = IsoDep.get(tag),
        scope = scope
    )

    private fun <T> onConnection(aid: ByteArray, func: (tag: IsoDep) -> T): NfcDataStream<T> =
        NfcDataStream<T>(initialValue = NfcResult(status = NfcDataStream.Status.CONNECTING))
            .connect(
                scope = scope,
                isoDep = isoDep,
                func = func,
                selectAid = {
                    val result = SelectAidCommand(aid).executeOnTag(isoDep)
                    if (!result.isSuccess) {
                        error("Could not select aid for 0x${aid.contentToString()}")
                    }
                }
            )

    /**
     * Send multiple commands to the NFC device in order and transform its result.
     *
     * @param commands The command list to be executed in order along with its transformation
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendCommandsTransforming(
        commands: List<Pair<ApduCommand, (result: ByteArray) -> T>>,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ): NfcDataStream<List<T>> = onConnection(aid = aid) {
        commands.map { (command, transformation) ->
            val result = command.executeOnTag(isoDep)
            when {
                result.isSuccess -> transformation.invoke(result)
                acceptEmpty && result.isEmpty() -> transformation.invoke(byteArrayOf())
                else -> error("Command failed")
            }
        }
    }

    /**
     * Send multiple commands to the NFC device in order.
     *
     * @param commands The command list to be executed in order
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun sendCommands(
        commands: List<ApduCommand>,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false
    ): NfcDataStream<List<ByteArray>> = sendCommandsTransforming(
        commands = commands.map { command -> command to { it } },
        aid = aid,
        acceptEmpty = acceptEmpty
    )

    /**
     * Send a command to the NFC device.
     *
     * @param command The command to be executed
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendData(
        command: ApduCommand,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (result: ByteArray) -> T
    ): NfcDataStream<List<T>> = sendCommandsTransforming(
        commands = listOf(command to transformResult),
        aid = aid,
        acceptEmpty = acceptEmpty
    )

    /**
     * Send a command to the NFC device.
     *
     * @param command The command to be executed
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun sendData(
        command: ApduCommand,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ): NfcDataStream<List<ByteArray>> =
        sendData(command = command, aid = aid, acceptEmpty = acceptEmpty, transformResult = { it })

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendData(
        content: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (result: ByteArray) -> T
    ): NfcDataStream<List<T>> = sendData(
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
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun sendData(
        content: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ): NfcDataStream<List<ByteArray>> = sendData(
        content = content,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = { it }
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transformResult A function that will transform the ByteArray to the desired result type
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendData(
        content: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transformResult: (result: ByteArray) -> T
    ): NfcDataStream<List<T>> = sendData(
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
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun sendData(
        content: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false
    ): NfcDataStream<List<ByteArray>> = sendData(
        content = content,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transformResult = { it }
    )
}
