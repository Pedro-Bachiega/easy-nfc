package com.pedrobneto.easynfc.initiator

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.pedrobneto.easynfc.asByteArray
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.ApduCommandHeader
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
        NfcDataStream<T>().connect(
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
    fun <T : Any, R : Any> sendCommandsTransforming(
        commands: List<Pair<ApduCommand, (result: ByteArray) -> T>>,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transform: (List<T>) -> R
    ): NfcDataStream<R> = onConnection(aid = aid) {
        commands.map { (command, transformation) ->
            val result = command.executeOnTag(isoDep)
            when {
                result.isSuccess -> transformation.invoke(result)
                acceptEmpty && result.isEmpty() -> transformation.invoke(byteArrayOf())
                else -> error("Command failed")
            }
        }.let(transform)
    }

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
    ): NfcDataStream<List<T>> = sendCommandsTransforming(
        commands = commands,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = { it }
    )

    /**
     * Send multiple commands to the NFC device in order.
     *
     * @param commands The command list to be executed in order
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendCommands(
        commands: List<ApduCommand>,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transform: (List<ByteArray>) -> T
    ): NfcDataStream<T> = sendCommandsTransforming(
        commands = commands.map { command -> command to { it } },
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = transform
    )

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
    ): NfcDataStream<List<ByteArray>> = sendCommands(
        commands = commands,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = { it }
    )

    /**
     * Send a command to the NFC device.
     *
     * @param command The command to be executed
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transform A function that will transform the ByteArray to the desired result type
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendData(
        command: ApduCommand,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transform: (result: ByteArray) -> T
    ): NfcDataStream<T> = sendCommands(
        commands = listOf(command),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = { transform.invoke(it.first()) }
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
    ): NfcDataStream<ByteArray> =
        sendData(command = command, aid = aid, acceptEmpty = acceptEmpty, transform = { it })

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param header The header for this command
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transform A function that will transform the ByteArray to the desired result type
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendData(
        header: ApduCommandHeader,
        content: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transform: (result: ByteArray) -> T
    ): NfcDataStream<T> = sendData(
        command = ApduCommand(header = header, content = content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = transform
    )

    /**
     * Send a command to the NFC device passing a ByteArray content.
     *
     * @param header The header for this command
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun sendData(
        header: ApduCommandHeader,
        content: ByteArray,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
    ): NfcDataStream<ByteArray> = sendData(
        header = header,
        content = content,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = { it }
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param header The header for this command
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     * @param transform A function that will transform the ByteArray to the desired result type
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun <T : Any> sendData(
        header: ApduCommandHeader,
        content: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false,
        transform: (result: ByteArray) -> T
    ): NfcDataStream<T> = sendData(
        command = ApduCommand(header = header, content = content),
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = transform
    )

    /**
     * Send a command to the NFC device passing a String content.
     *
     * @param header The header for this command
     * @param content The content of the command
     * @param aid The AID of the NFC device - Defaults to the aid provided to the NfcHelper
     * @param acceptEmpty Whether to accept an empty response or consider it an error
     *
     * @return The [NfcDataStream] with the result in a [kotlinx.coroutines.flow.Flow] or [androidx.lifecycle.LiveData]
     */
    fun sendData(
        header: ApduCommandHeader,
        content: String,
        aid: ByteArray = this.aid,
        acceptEmpty: Boolean = false
    ): NfcDataStream<ByteArray> = sendData(
        header = header,
        content = content,
        aid = aid,
        acceptEmpty = acceptEmpty,
        transform = { it }
    )
}
