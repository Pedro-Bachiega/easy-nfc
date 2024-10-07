package com.pedrobneto.easynfc.handler

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.pedrobneto.easynfc.model.ApduCommandHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val LOG_TAG = "NfcHandlerService"

abstract class NfcHandlerService : HostApduService() {

    private val job = SupervisorJob()
    protected val scope = CoroutineScope(Dispatchers.IO + job)

    protected val successResult: ByteArray get() = byteArrayOf(0x90.toByte(), 0x00.toByte())

    private var composedContent: ByteArray = byteArrayOf()

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        val header = ApduCommandHeader.fromByteArray(commandApdu)
        if (header == ApduCommandHeader.selectAid) return successResult

        val needMoreData = needMoreData(header)

        scope.launch {
            runCatching {
                // Byte 5 is the content length, so we can ignore
                composedContent += commandApdu.takeLast(commandApdu.size - 5)

                if (!needMoreData) {
                    onCommandReceived(header, composedContent)
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
    abstract fun needMoreData(header: ApduCommandHeader): Boolean

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
     * @param header The command header
     * @param content The content of the command
     */
    open fun onCommandReceived(header: ApduCommandHeader, content: ByteArray) =
        onCommandReceived(header, content.decodeToString())

    /**
     * Implement this method whenever you want to deal with the content as a String.
     * This method will be called in the IO thread.
     *
     * @param header The command header
     * @param content The content of the command
     */
    open fun onCommandReceived(header: ApduCommandHeader, content: String) {
        error("You must implement 'onCommandReceived' for either String OR ByteArray content")
    }
}
