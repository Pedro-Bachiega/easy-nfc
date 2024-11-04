package com.pedrobneto.easynfc.handler

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.ApduCommandHeader
import com.pedrobneto.easynfc.model.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val LOG_TAG = "NfcHandlerService"

/**
 * A base service class that will handle all operations and give you the final content
 *
 * @see com.pedrobneto.easynfc.initiator.NfcHelper
 */
abstract class NfcHandlerService : HostApduService() {

    private val job = SupervisorJob()
    protected val scope = CoroutineScope(Dispatchers.IO + job)

    /**
     * The default success response as a [ByteArray]
     */
    protected val successResult: ByteArray get() = byteArrayOf(0x90.toByte(), 0x00.toByte())

    private var composedCommand: ApduCommand? = null

    /**
     * The root function where we receive the command from the reader
     *
     * This method is called in the main thread. DO NOT take long processing and responding.
     *
     * @see onDeactivated If you want to handle the service deactivation
     * @see onCommandReceived For handling the processed content
     * @see needMoreData To tell if we're dealing with a multi-part content
     * @see onNeedMoreData To answer the question "Do we need more data?" to the reader. Defaults to `[0x90, 0x00]`
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        val command = ApduCommand(commandApdu)
        val header = command.getHeader()
        if (header == ApduCommandHeader.selectAid) return successResult

        val needMoreData = needMoreData(header)

        scope.launch {
            runCatching {
                composedCommand = composedCommand?.let { onMergeCommands(it, command) } ?: command

                if (!needMoreData) {
                    onCommandReceived(composedCommand!!)
                    composedCommand = null
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
     *
     * @param header The command header
     */
    open fun needMoreData(header: ApduCommandHeader): Boolean = false

    /**
     * This method answers the question "Do we need more data?" to the reader.
     * The default response is `[0x90, 0x00]` which means success
     *
     * This method is called in the main thread. DO NOT take long processing and responding.
     *
     * @see [successResult]
     */
    open fun onNeedMoreData(): ByteArray = successResult

    /**
     * This method is responsible for merging 2 commands whenever a composition is needed.
     * The default response is the sum of the 2 commands using the implemented operator [com.pedrobneto.easynfc.model.plus].
     * Override if you need a custom treatment whenever you're handling a multi-part content.
     *
     * @param current The current command being stored and waiting for the next part
     * @param next The next part of the content
     *
     * @return The merged commands
     */
    open fun onMergeCommands(current: ApduCommand, next: ApduCommand): ApduCommand =
        current + next

    /**
     * Implement this method whenever you want to deal with the content as a ByteArray.
     * This method will be called in the IO thread.
     *
     * @param apduCommand The command object with custom getters for ease of use :)
     */
    abstract fun onCommandReceived(apduCommand: ApduCommand)
}
