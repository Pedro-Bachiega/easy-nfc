package com.pedrobneto.sample.easynfc.handler

import android.content.Intent
import android.util.Log
import com.pedrobneto.easynfc.handler.NfcHandlerService
import com.pedrobneto.easynfc.model.ApduCommand
import com.pedrobneto.easynfc.model.ApduCommandHeader
import com.pedrobneto.sample.easynfc.initiator.ReceiverActivity

internal class SampleNfcHandlerService : NfcHandlerService() {

    // Just set whatever condition you want, you'll only need to use it if you use multipart content
    // In this case, we'll consider we're waiting for more content ONLY if the header is a update binary header
    // If not using, just don't override
    override fun needMoreData(header: ApduCommandHeader): Boolean =
        header == ApduCommandHeader.updateBinary

    override fun onCommandReceived(command: ApduCommand) {
        val content = command.getDataAsString()

        // Do something with the content
        Log.d("SampleNfcHandlerService", "Received from nfc reader:\n$content")

        startActivity(
            Intent(this, ReceiverActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(ReceiverActivity.EXTRA_NFC_DATA, content)
        )
    }
}
