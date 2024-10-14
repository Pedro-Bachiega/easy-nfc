package com.pedrobneto.sample.easynfc.handler

import android.util.Log
import android.widget.Toast
import com.pedrobneto.easynfc.handler.NfcHandlerService
import com.pedrobneto.easynfc.model.ApduCommandHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SampleNfcHandlerService : NfcHandlerService() {

    // Just set whatever byte you want as instruction, you'll only need to use it if you use multipart content
    // If not using, just override
    private val multipartInstruction = 0x10.toByte()

    // "[0x00, 0x01]" will mean we have more data to receive, "[0x00, 0x00]" will mean we're done
    private val multipartParameter1 = 0x00.toByte()
    private val multipartParameter2 = 0x01.toByte()

    override fun needMoreData(header: ApduCommandHeader): Boolean =
        header.instruction == multipartInstruction
                && header.parameter1 == multipartParameter1
                && header.parameter2 == multipartParameter2

    override fun onCommandReceived(header: ApduCommandHeader, content: String) {
        // Do something with the content
        Log.d("SampleNfcHandlerService", "Received from nfc reader:\n$content")

        scope.launch(Dispatchers.Main) {
            Toast.makeText(this@SampleNfcHandlerService, content, Toast.LENGTH_SHORT).show()
        }
    }
}
