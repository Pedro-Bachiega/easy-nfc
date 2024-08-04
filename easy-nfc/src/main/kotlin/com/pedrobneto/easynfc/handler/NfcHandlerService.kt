package com.pedrobneto.easynfc.handler

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val LOG_TAG = "NfcHandlerService"

private val ByteArray?.isSelectAidCommand: Boolean
    get() = this != null &&
            this[0] == 0x00.toByte() &&
            this[1] == 0xA4.toByte() &&
            this[2] == 0x04.toByte() &&
            this[3] == 0x00.toByte()

abstract class NfcHandlerService : HostApduService() {

    private val job = SupervisorJob()
    protected val scope = CoroutineScope(Dispatchers.IO + job)

    protected val successResult: ByteArray get() = byteArrayOf(0x90.toByte(), 0x00.toByte())

    private var composedContent: ByteArray = byteArrayOf()

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        if (commandApdu.isSelectAidCommand) return successResult

        val clazz = commandApdu[0]
        val instruction = commandApdu[1]
        val parameter1 = commandApdu[2]
        val parameter2 = commandApdu[3]

        val needMoreData = needMoreData(instruction, parameter1, parameter2)

        scope.launch {
            runCatching {
                // Byte 5 is the content length, so we can ignore
                composedContent += commandApdu.takeLast(commandApdu.size - 5)

                if (!needMoreData) {
                    onCommandReceived(clazz, instruction, parameter1, parameter2, composedContent)
                    composedContent = byteArrayOf()
                }
            }.onFailure {
                Log.e(LOG_TAG, "Error processing command", it)
            }
        }

        return if (needMoreData) onNeedMoreData() else successResult
    }

    override fun onDeactivated(reason: Int) {
        Log.d(LOG_TAG, "Service deactivated, reason: $reason")
    }

    /**
     * This method tells if we're dealing with a multi-part content.
     * Since the apdu content length is limited, whenever we want to send long arrays we MUST
     * slice it to multiple parts before sending.
     *
     * This method is called in the main thread. DO NOT take long processing and responding.
     */
    abstract fun needMoreData(instruction: Byte, parameter1: Byte, parameter2: Byte): Boolean

    /**
     * This method answers the question "Do we need more data?" to the reader.
     * Default response is "[0x90, 0x00]" which means success
     *
     * This method is called in the main thread. DO NOT take long processing and responding.
     */
    open fun onNeedMoreData(): ByteArray = successResult

    /**
     * Implement this method whenever you want to deal with the content as a ByteArray.
     * This method will be called in the IO thread.
     *
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command
     * @param parameter2 The fourth byte of the command
     * @param content The content of the command
     */
    open fun onCommandReceived(
        clazz: Byte,
        instruction: Byte,
        parameter1: Byte,
        parameter2: Byte,
        content: ByteArray
    ) = onCommandReceived(clazz, instruction, parameter1, parameter2, content.decodeToString())

    /**
     * Implement this method whenever you want to deal with the content as a String.
     * This method will be called in the IO thread.
     *
     * @param clazz The first byte of the command
     * @param instruction The second byte of the command
     * @param parameter1 The third byte of the command
     * @param parameter2 The fourth byte of the command
     * @param content The content of the command
     */
    open fun onCommandReceived(
        clazz: Byte,
        instruction: Byte,
        parameter1: Byte,
        parameter2: Byte,
        content: String
    ) {
        error("You must implement 'onCommandReceived' for either String OR ByteArray content")
    }
}
